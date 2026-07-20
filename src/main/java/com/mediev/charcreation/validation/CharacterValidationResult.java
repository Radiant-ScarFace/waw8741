package com.mediev.charcreation.validation;

public final class CharacterValidationResult {
    private final boolean valid;
    private final String errorMessage;

    private CharacterValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public static CharacterValidationResult valid() {
        return new CharacterValidationResult(true, "");
    }

    public static CharacterValidationResult invalid(String errorMessage) {
        return new CharacterValidationResult(false, errorMessage);
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}