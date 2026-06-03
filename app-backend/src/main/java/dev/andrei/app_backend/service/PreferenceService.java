package dev.andrei.app_backend.service;

import dev.andrei.app_backend.dto.preference.AttributeConceptDto;
import dev.andrei.app_backend.dto.preference.PreferenceUpdateDto;
import dev.andrei.app_backend.model.AttributeConcept;
import dev.andrei.app_backend.model.UserAttributePreference;
import dev.andrei.app_backend.repository.AttributeConceptRepository;
import dev.andrei.app_backend.repository.UserAttributePreferenceRepository;
import dev.andrei.app_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PreferenceService {

    private final AttributeConceptRepository conceptRepository;
    private final UserAttributePreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public PreferenceService(AttributeConceptRepository conceptRepository,
                             UserAttributePreferenceRepository preferenceRepository,
                             UserRepository userRepository) {
        this.conceptRepository = conceptRepository;
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
    }

    // All concepts (ordered for display) merged with the user's current importance (0 when unset).
    @Transactional(readOnly = true)
    public List<AttributeConceptDto> getUserPreferences(UUID userId) {
        Map<UUID, Integer> importanceByConcept = preferenceRepository.findByUser_Id(userId).stream()
                .collect(Collectors.toMap(p -> p.getConcept().getId(),
                        UserAttributePreference::getImportance, (a, b) -> a));

        return conceptRepository.findAllByOrderBySortOrderAsc().stream()
                .map(c -> new AttributeConceptDto(
                        c.getId(),
                        c.getSlug(),
                        c.getDisplayName(),
                        c.getGroupLabel(),
                        c.getSortOrder(),
                        importanceByConcept.getOrDefault(c.getId(), 0)))
                .toList();
    }

    // Upsert each (user, concept) importance. Unknown concept ids are ignored.
    @Transactional
    public void updatePreferences(UUID userId, List<PreferenceUpdateDto> updates) {
        for (PreferenceUpdateDto update : updates) {
            UUID conceptId = update.conceptId();
            int importance = update.importance();

            UserAttributePreference existing = preferenceRepository
                    .findByUser_IdAndConcept_Id(userId, conceptId)
                    .orElse(null);

            if (existing != null) {
                existing.setImportance(importance);
                preferenceRepository.save(existing);
            } else if (conceptRepository.existsById(conceptId)) {
                UserAttributePreference pref = new UserAttributePreference();
                pref.setId(UUID.randomUUID());
                pref.setUser(userRepository.getReferenceById(userId));
                pref.setConcept(conceptRepository.getReferenceById(conceptId));
                pref.setImportance(importance);
                preferenceRepository.save(pref);
            }
            // else: unknown concept id -> skip silently.
        }
    }
}
