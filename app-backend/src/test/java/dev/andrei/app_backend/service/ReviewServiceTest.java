package dev.andrei.app_backend.service;

import dev.andrei.app_backend.dto.review.AttributeRatingDto;
import dev.andrei.app_backend.dto.review.MyReviewDto;
import dev.andrei.app_backend.dto.review.ReviewDto;
import dev.andrei.app_backend.dto.review.SubmitReviewRequest;
import dev.andrei.app_backend.model.Attribute;
import dev.andrei.app_backend.model.Location;
import dev.andrei.app_backend.model.LocationAttribute;
import dev.andrei.app_backend.model.Review;
import dev.andrei.app_backend.model.ReviewAttributeScore;
import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.repository.LocationRepository;
import dev.andrei.app_backend.repository.ReviewRepository;
import dev.andrei.app_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private final UUID userId = UUID.randomUUID();
    private final UUID locationId = UUID.randomUUID();

    private Location location;
    private LocationAttribute cleanliness; // weight 1, avg 4.0, count 1
    private LocationAttribute service;     // weight 2, avg 3.0, count 1
    private LocationAttribute atmosphere;  // weight 1, never rated (avg null, count 0)

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setId(locationId);
        location.setName("Test Park");
        cleanliness = locationAttribute("Cleanliness", 1.0, 4.0, 1);
        service = locationAttribute("Service", 2.0, 3.0, 1);
        atmosphere = locationAttribute("Atmosphere", 1.0, null, 0);
        location.setLocationAttributes(new HashSet<>(List.of(cleanliness, service, atmosphere)));
    }

    @Test
    void submitReview_whenValid_persistsReviewAndUpdatesAggregates() {
        Mockito.when(locationRepository.findWithAttributesById(locationId))
                .thenReturn(Optional.of(location));
        Mockito.when(reviewRepository.existsByUser_IdAndLocation_Id(userId, locationId))
                .thenReturn(false);
        Mockito.when(userRepository.getReferenceById(userId))
                .thenReturn(new User(userId, "user@test.com", "hash", Instant.now()));

        SubmitReviewRequest request = request("  Lovely spot  ",
                new AttributeRatingDto("Cleanliness", 5.0),
                new AttributeRatingDto("Service", 4.0),
                new AttributeRatingDto("Atmosphere", 2.0));

        reviewService.submitReview(userId, request);

        // --- the persisted review ---
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        Mockito.verify(reviewRepository).save(captor.capture());
        Review saved = captor.getValue();

        assertNotNull(saved.getId());
        assertEquals(userId, saved.getUser().getId());
        assertSame(location, saved.getLocation());
        assertEquals("Lovely spot", saved.getContent()); // trimmed
        assertNotNull(saved.getCreatedAt());
        assertEquals(3, saved.getAttributeScores().size());
        saved.getAttributeScores().forEach(s -> assertSame(saved, s.getReview()));

        // --- per-attribute running averages ---
        assertEquals(2, cleanliness.getScore_count().intValue());
        assertEquals(4.5, cleanliness.getAverage_score(), 1e-9);  // (4*1 + 5) / 2
        assertEquals(2, service.getScore_count().intValue());
        assertEquals(3.5, service.getAverage_score(), 1e-9);      // (3*1 + 4) / 2
        assertEquals(1, atmosphere.getScore_count().intValue());
        assertEquals(2.0, atmosphere.getAverage_score(), 1e-9);   // first ever rating

        // --- overall weighted average: (4.5*1 + 3.5*2 + 2.0*1) / (1+2+1) = 13.5 / 4 ---
        assertEquals(3.375, location.getAverage_score(), 1e-9);
    }

    @Test
    void submitReview_whenLocationMissing_throwsLocationNotFound() {
        Mockito.when(locationRepository.findWithAttributesById(locationId))
                .thenReturn(Optional.empty());

        assertThrows(ReviewService.LocationNotFoundException.class,
                () -> reviewService.submitReview(userId,
                        request("x", new AttributeRatingDto("Cleanliness", 5.0))));

        Mockito.verify(reviewRepository, Mockito.never()).save(any());
    }

    @Test
    void submitReview_whenAlreadyReviewed_throwsAlreadyReviewed() {
        Mockito.when(locationRepository.findWithAttributesById(locationId))
                .thenReturn(Optional.of(location));
        Mockito.when(reviewRepository.existsByUser_IdAndLocation_Id(userId, locationId))
                .thenReturn(true);

        assertThrows(ReviewService.AlreadyReviewedException.class,
                () -> reviewService.submitReview(userId,
                        request("x", new AttributeRatingDto("Cleanliness", 5.0))));

        Mockito.verify(reviewRepository, Mockito.never()).save(any());
    }

    @Test
    void submitReview_whenAttributeNotOnLocation_throwsInvalidReview() {
        Mockito.when(locationRepository.findWithAttributesById(locationId))
                .thenReturn(Optional.of(location));
        Mockito.when(reviewRepository.existsByUser_IdAndLocation_Id(userId, locationId))
                .thenReturn(false);

        assertThrows(ReviewService.InvalidReviewException.class,
                () -> reviewService.submitReview(userId,
                        request("x", new AttributeRatingDto("Wifi", 5.0))));

        Mockito.verify(reviewRepository, Mockito.never()).save(any());
    }

    @Test
    void submitReview_whenRatingIsNotAHalfStep_throwsInvalidReview() {
        Mockito.when(locationRepository.findWithAttributesById(locationId))
                .thenReturn(Optional.of(location));
        Mockito.when(reviewRepository.existsByUser_IdAndLocation_Id(userId, locationId))
                .thenReturn(false);

        assertThrows(ReviewService.InvalidReviewException.class,
                () -> reviewService.submitReview(userId,
                        request("x", new AttributeRatingDto("Cleanliness", 3.3))));

        Mockito.verify(reviewRepository, Mockito.never()).save(any());
    }

    @Test
    void getLocationReviews_mapsOverallScoreAndDisplayName() {
        User author = new User(UUID.randomUUID(), "a@t.com", "h", Instant.now());
        author.setDisplayName("Alice");
        Review review = review(author, "Great",
                cleanliness.getAttribute(), 5.0,
                service.getAttribute(), 4.0);
        Mockito.when(reviewRepository.findByLocationIdWithScores(locationId)).thenReturn(List.of(review));

        List<ReviewDto> result = reviewService.getLocationReviews(locationId);

        assertEquals(1, result.size());
        ReviewDto dto = result.get(0);
        assertEquals("Alice", dto.reviewerDisplayName());
        assertEquals(4.5, dto.overallScore(), 1e-9); // (5 + 4) / 2
        assertEquals(2, dto.attributeScores().size());
    }

    @Test
    void getLocationReviews_whenDisplayNameNull_fallsBackToAnonymous() {
        User author = new User(UUID.randomUUID(), "a@t.com", "h", Instant.now()); // null display name
        Review review = review(author, "ok", cleanliness.getAttribute(), 3.0);
        Mockito.when(reviewRepository.findByLocationIdWithScores(locationId)).thenReturn(List.of(review));

        assertEquals("Anonymous", reviewService.getLocationReviews(locationId).get(0).reviewerDisplayName());
    }

    @Test
    void getMyReviews_includesLocation() {
        User author = new User(userId, "a@t.com", "h", Instant.now());
        author.setDisplayName("Me");
        Review review = review(author, "mine", cleanliness.getAttribute(), 4.0);
        Mockito.when(reviewRepository.findByUserIdWithScores(userId)).thenReturn(List.of(review));

        MyReviewDto dto = reviewService.getMyReviews(userId).get(0);
        assertEquals(locationId, dto.locationId());
        assertEquals("Test Park", dto.locationName());
        assertEquals(4.0, dto.overallScore(), 1e-9);
    }

    private Review review(User author, String content, Object... attributeScorePairs) {
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setUser(author);
        review.setLocation(location);
        review.setContent(content);
        review.setCreatedAt(Instant.now());
        for (int i = 0; i < attributeScorePairs.length; i += 2) {
            ReviewAttributeScore score = new ReviewAttributeScore();
            score.setId(UUID.randomUUID());
            score.setAttribute((Attribute) attributeScorePairs[i]);
            score.setScore((Double) attributeScorePairs[i + 1]);
            review.addAttributeScore(score);
        }
        return review;
    }

    private LocationAttribute locationAttribute(String name, double weight, Double avg, Integer count) {
        Attribute attribute = new Attribute();
        attribute.setId(UUID.randomUUID());
        attribute.setName(name);
        attribute.setGlobal_weight(weight);

        LocationAttribute la = new LocationAttribute();
        la.setId(UUID.randomUUID());
        la.setAttribute(attribute);
        la.setLocation(location);
        la.setAverage_score(avg);
        la.setScore_count(count);
        return la;
    }

    private SubmitReviewRequest request(String content, AttributeRatingDto... ratings) {
        return new SubmitReviewRequest(locationId, content, List.of(ratings));
    }
}
