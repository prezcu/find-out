package dev.andrei.app_backend.config;

import dev.andrei.app_backend.service.TextNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Locations are loaded from outside this service and existed before the
 * normalized_name column was added. {@code @Immutable} blocks Hibernate from
 * updating them, so we backfill once at startup using raw SQL.
 *
 * <p>Idempotent: only touches rows where normalized_name IS NULL.
 */
@Component
public class NormalizedNameBackfill {

    private static final Logger log = LoggerFactory.getLogger(NormalizedNameBackfill.class);

    private final JdbcTemplate jdbcTemplate;

    public NormalizedNameBackfill(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void backfill() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name FROM location WHERE normalized_name IS NULL");

        if (rows.isEmpty()) {
            return;
        }

        for (Map<String, Object> row : rows) {
            UUID id = (UUID) row.get("id");
            String name = (String) row.get("name");
            String normalized = TextNormalizer.normalize(name);
            jdbcTemplate.update(
                    "UPDATE location SET normalized_name = ? WHERE id = ?",
                    normalized, id);
        }

        log.info("Backfilled normalized_name for {} location row(s)", rows.size());
    }
}
