package com.mediev.charcreation.validation;

public final class CharacterValidator {
    private CharacterValidator() {
    }

    public static CharacterValidationResult validate(String firstName, String lastName, int birthDay, int birthMonth, int birthYear) {
        CharacterValidationResult firstNameResult = NameValidator.validate(firstName, "First name");
        if (!firstNameResult.isValid()) {
            return firstNameResult;
        }
        CharacterValidationResult lastNameResult = NameValidator.validate(lastName, "Last name");
        if (!lastNameResult.isValid()) {
            return lastNameResult;
        }
        return DateOfBirthValidator.validate(birthDay, birthMonth, birthYear);
    }
}