package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.dto.review.SubmitReviewRequest;
import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Auth is enforced by SecurityConfig (.anyRequest().authenticated()), so `user` is never null;
    // an absent/invalid token yields a 401 before reaching here.
    @PostMapping
    public ResponseEntity<Void> submitReview(@AuthenticationPrincipal User user,
                                             @Valid @RequestBody SubmitReviewRequest request) {
        reviewService.submitReview(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
