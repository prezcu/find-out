package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.service.AuthService;
import dev.andrei.app_backend.service.ReviewService;
import dev.andrei.app_backend.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 401 ---
    @ExceptionHandler(AuthService.InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials() {
        return problem(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password");
    }

    // --- 404 ---
    @ExceptionHandler({WishlistService.LocationNotFoundException.class, ReviewService.LocationNotFoundException.class})
    public ProblemDetail handleLocationNotFound() {
        return problem(HttpStatus.NOT_FOUND, "LOCATION_NOT_FOUND", "This location has not been found");
    }

    // --- 409 ---
    @ExceptionHandler(AuthService.EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists() {
        return problem(HttpStatus.CONFLICT, "EMAIL_TAKEN", "This email is already registered");
    }

    @ExceptionHandler(WishlistService.AlreadyWishlistedException.class)
    public ProblemDetail handleAlreadyWishlisted() {
        return problem(HttpStatus.CONFLICT, "ALREADY_WISHLISTED", "This location is already in the user's wishlist");
    }

    @ExceptionHandler(ReviewService.AlreadyReviewedException.class)
    public ProblemDetail handleAlreadyReviewed() {
        return problem(HttpStatus.CONFLICT, "ALREADY_REVIEWED", "This user has already reviewed the location");
    }

    // --- 400 ---
    @ExceptionHandler({MethodArgumentNotValidException.class, ReviewService.InvalidReviewException.class})
    public ProblemDetail handleFailedValidation() {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "The data is not valid for this request");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedRequest() {
        return problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "The request body could not be read");
    }

    private ProblemDetail problem(HttpStatus status, String errorCode, String detail) {
        ProblemDetail newDetail = ProblemDetail.forStatusAndDetail(status, detail);
        newDetail.setProperty("error", errorCode);
        return newDetail;
    }
}
