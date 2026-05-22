package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.dto.auth.AuthResponse;
import dev.andrei.app_backend.dto.auth.ChangePasswordRequest;
import dev.andrei.app_backend.dto.auth.LoginRequest;
import dev.andrei.app_backend.dto.auth.RegisterRequest;
import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse body = authService.register(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse body = authService.login(request.email(), request.password());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user.getId(), request.oldPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
