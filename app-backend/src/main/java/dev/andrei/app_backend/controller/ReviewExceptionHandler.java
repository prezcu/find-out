package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ReviewExceptionHandler {

    // Bean-validation failures (MethodArgumentNotValidException) are already mapped to
    // 400 VALIDATION_FAILED by AuthExceptionHandler, which applies globally.

    @ExceptionHandler(ReviewService.LocationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleLocationNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "LOCATION_NOT_FOUND"));
    }

    @ExceptionHandler(ReviewService.AlreadyReviewedException.class)
    public ResponseEntity<Map<String, String>> handleAlreadyReviewed() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "ALREADY_REVIEWED"));
    }

    @ExceptionHandler(ReviewService.InvalidReviewException.class)
    public ResponseEntity<Map<String, String>> handleInvalidReview() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "VALIDATION_FAILED"));
    }
}