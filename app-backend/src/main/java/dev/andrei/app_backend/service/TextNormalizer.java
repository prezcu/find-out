package dev.andrei.app_backend.service;

import java.text.Normalizer;
import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {}

    /**
     * Trim, collapse internal whitespace to single spaces, lowercase under
     * Locale.ROOT, then strip diacritics
     * <p>
     * Returns "" for null/blank input — callers can short-circuit on isBlank().
     */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String collapsed = input.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return "";
        }
        String lowered = collapsed.toLowerCase(Locale.ROOT);
        String decomposed = Normalizer.normalize(lowered, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
