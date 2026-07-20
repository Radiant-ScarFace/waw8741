package com.mediev.charcreation.validation;

import java.util.regex.Pattern;

public final class NameValidator {
    private static final Pattern LETTERS_ONLY = Pattern.compile("^[A-Za-z]{2,16}$");

    private NameValidator() {
    }

    public static CharacterValidationResult validate(String rawName, String fieldLabel) {
        if (rawName == null) {
            return CharacterValidationResult.invalid(fieldLabel + " is required.");
        }
        String trimmed = rawName.trim();
        if (!LETTERS_ONLY.matcher(trimmed).matches()) {
            return CharacterValidationResult.invalid(fieldLabel + " must be 2-16 letters only.");
        }
        return CharacterValidationResult.valid();
    }

    public static String normalize(String rawName) {
        String trimmed = rawName.trim().toLowerCase();
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }
}