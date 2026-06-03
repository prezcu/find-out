package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class WishlistExceptionHandler {

    @ExceptionHandler(WishlistService.LocationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleLocationNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "LOCATION_NOT_FOUND"));
    }

    @ExceptionHandler(WishlistService.AlreadyWishlistedException.class)
    public ResponseEntity<Map<String, String>> handleAlreadyWishlisted() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "ALREADY_WISHLISTED"));
    }
}