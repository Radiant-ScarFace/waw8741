package com.mediev.charcreation.validation;

import java.time.DateTimeException;
import java.time.LocalDate;

public final class DateOfBirthValidator {
    private static final int MIN_YEAR = 1000;
    private static final int MAX_YEAR = 1600;

    private DateOfBirthValidator() {
    }

    public static CharacterValidationResult validate(int day, int month, int year) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            return CharacterValidationResult.invalid("Birth year must be between " + MIN_YEAR + " and " + MAX_YEAR + ".");
        }
        try {
            LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            return CharacterValidationResult.invalid("Date of birth is not a valid calendar date.");
        }
        return CharacterValidationResult.valid();
    }
}