package dev.andrei.app_backend.dto.auth;

public record AuthResponse(String token, long expiresAt) {}
