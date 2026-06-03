package dev.andrei.app_backend.service;

import dev.andrei.app_backend.dto.review.AttributeRatingDto;
import dev.andrei.app_backend.dto.review.AttributeScoreDto;
import dev.andrei.app_backend.dto.review.MyReviewDto;
import dev.andrei.app_backend.dto.review.ReviewDto;
import dev.andrei.app_backend.dto.review.SubmitReviewRequest;
import dev.andrei.app_backend.model.Location;
import dev.andrei.app_backend.model.LocationAttribute;
import dev.andrei.app_backend.model.Review;
import dev.andrei.app_backend.model.ReviewAttributeScore;
import dev.andrei.app_backend.repository.LocationRepository;
import dev.andrei.app_backend.repository.ReviewRepository;
import dev.andrei.app_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         LocationRepository locationRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void submitReview(UUID userId, SubmitReviewRequest request) {
        // Fetch the location together with its per-attribute aggregate rows so we can both
        // validate the incoming attribute names and update the running averages in place.
        Location location = locationRepository.findWithAttributesById(request.locationId())
                .orElseThrow(LocationNotFoundException::new);

        if (reviewRepository.existsByUser_IdAndLocation_Id(userId, request.locationId())) {
            throw new AlreadyReviewedException();
        }

        Map<String, LocationAttribute> aggregatesByName = new HashMap<>();
        for (LocationAttribute la : location.getLocationAttributes()) {
            aggregatesByName.put(la.getAttribute().getName(), la);
        }

        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setUser(userRepository.getReferenceById(userId)); // FK reference, no extra SELECT
        review.setLocation(location);
        review.setContent(request.content() == null ? "" : request.content().trim());
        review.setCreatedAt(Instant.now());

        // Accumulate this review's own weighted average while validating each rating:
        // averageScore = sum(rating * attribute weight) / sum(weight), mirroring how a location's
        // overall score is weighted (see recomputeLocationAverage).
        double weightedSum = 0.0;
        double weightTotal = 0.0;

        for (AttributeRatingDto incoming : request.attributeRatings()) {
            LocationAttribute aggregate = aggregatesByName.get(incoming.attribute());
            if (aggregate == null) {
                throw new InvalidReviewException(
                        "Unknown attribute for this location: " + incoming.attribute());
            }
            double rating = incoming.rating();
            if (!isHalfStep(rating)) {
                throw new InvalidReviewException("Rating must be between 0.5 and 5.0 in 0.5 steps");
            }

            double weight = aggregate.getAttribute().getGlobal_weight();
            weightedSum += rating * weight;
            weightTotal += weight;

            ReviewAttributeScore attributeScore = new ReviewAttributeScore();
            attributeScore.setId(UUID.randomUUID());
            attributeScore.setAttribute(aggregate.getAttribute());
            attributeScore.setScore(rating);
            review.addAttributeScore(attributeScore);

            applyRatingToAggregate(aggregate, rating);
        }

        // Guard against a zero total weight (e.g. all-zero attribute weights) to avoid NaN.
        review.setAverageScore(weightTotal > 0 ? weightedSum / weightTotal : 0.0);

        reviewRepository.save(review);
        recomputeLocationAverage(location);
        // The Location + LocationAttribute mutations are managed entities, so they flush on commit.
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getLocationReviews(UUID locationId) {
        return reviewRepository.findByLocationIdWithScores(locationId).stream()
                .map(this::toReviewDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReviewDto> getMyReviews(UUID userId) {
        return reviewRepository.findByUserIdWithScores(userId).stream()
                .map(this::toMyReviewDto)
                .toList();
    }

    private ReviewDto toReviewDto(Review review) {
        List<AttributeScoreDto> scores = attributeScores(review);
        String name = review.getUser().getDisplayName();
        return new ReviewDto(
                review.getId(),
                (name == null || name.isBlank()) ? "Anonymous" : name,
                review.getContent(),
                review.getCreatedAt(),
                review.getAverageScore(),
                scores
        );
    }

    private MyReviewDto toMyReviewDto(Review review) {
        List<AttributeScoreDto> scores = attributeScores(review);
        return new MyReviewDto(
                review.getId(),
                review.getLocation().getId(),
                review.getLocation().getName(),
                review.getContent(),
                review.getCreatedAt(),
                review.getAverageScore(),
                scores
        );
    }

    private List<AttributeScoreDto> attributeScores(Review review) {
        return review.getAttributeScores().stream()
                .map(s -> new AttributeScoreDto(
                        s.getAttribute().getName(),
                        s.getScore()))
                .toList();
    }

    /** Folds one new rating into a per-attribute running average. */
    private void applyRatingToAggregate(LocationAttribute aggregate, double rating) {
        int oldCount = aggregate.getScore_count() == null ? 0 : aggregate.getScore_count();
        double oldAvg = aggregate.getAverage_score() == null ? 0.0 : aggregate.getAverage_score();

        int newCount = oldCount + 1;
        double newAvg = (oldAvg * oldCount + rating) / newCount;

        aggregate.setScore_count(newCount);
        aggregate.setAverage_score(newAvg);
    }

    /** Overall score = the location's attribute averages weighted by each attribute's global weight. */
    private void recomputeLocationAverage(Location location) {
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        for (LocationAttribute la : location.getLocationAttributes()) {
            boolean rated = la.getScore_count() != null && la.getScore_count() > 0
                    && la.getAverage_score() != null;
            if (rated) {
                double weight = la.getAttribute().getGlobal_weight();
                weightedSum += la.getAverage_score() * weight;
                weightTotal += weight;
            }
        }
        if (weightTotal > 0) {
            location.setAverage_score(weightedSum / weightTotal);
        }
    }

    /** True when rating is in [0.5, 5.0] and a multiple of 0.5. */
    private static boolean isHalfStep(double rating) {
        if (rating < 0.5 || rating > 5.0) return false;
        double doubled = rating * 2.0;
        return Math.abs(doubled - Math.rint(doubled)) < 1e-9;
    }

    // Nested exceptions, mirroring AuthService. Mapped to HTTP codes in ReviewExceptionHandler.
    public static class LocationNotFoundException extends RuntimeException {}
    public static class AlreadyReviewedException extends RuntimeException {}
    public static class InvalidReviewException extends RuntimeException {
        public InvalidReviewException(String message) { super(message); }
    }
}
