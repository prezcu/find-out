-- Typo-tolerant search support (run once, with a privileged DB role).
--
-- The schema is externally managed (ddl-auto=validate, no Flyway/Liquibase), so this is
-- applied by hand. `validate` only checks tables/columns/types, not indexes, so adding the
-- GIN index below does not affect application startup.

-- Trigram similarity functions + operators (similarity(), the `%` operator, etc.).
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- GIN trigram index accelerates BOTH `normalized_name % :q` (similarity) and `LIKE '%q%'`.
CREATE INDEX IF NOT EXISTS idx_location_normalized_name_trgm
    ON location USING gin (normalized_name gin_trgm_ops);

-- Optional: the category set is small, so non-indexed scans on it are cheap. Add only if
-- category fuzzy matching becomes a hotspot.
-- CREATE INDEX IF NOT EXISTS idx_location_primary_category_trgm
--     ON location USING gin (lower(primary_category) gin_trgm_ops);
