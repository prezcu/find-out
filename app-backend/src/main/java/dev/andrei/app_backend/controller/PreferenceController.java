package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.dto.preference.AttributeConceptDto;
import dev.andrei.app_backend.dto.preference.UpdatePreferencesRequest;
import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.service.PreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    // The user's concept list with their current importance values (auth required).
    @GetMapping
    public ResponseEntity<List<AttributeConceptDto>> getPreferences(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(preferenceService.getUserPreferences(user.getId()));
    }

    @PutMapping
    public ResponseEntity<Void> updatePreferences(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody UpdatePreferencesRequest request) {
        preferenceService.updatePreferences(user.getId(), request.preferences());
        return ResponseEntity.noContent().build();
    }
}
