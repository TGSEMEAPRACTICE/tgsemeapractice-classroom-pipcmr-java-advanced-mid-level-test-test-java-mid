package com.teksystems.pip.midtest.validation;

import java.util.Objects;

public final class ValidationResult {
    private final boolean valid;
    private final String message;

    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public static ValidationResult valid(String message) {
        return new ValidationResult(true, Objects.requireNonNull(message));
    }

    public static ValidationResult valid() {
        return new ValidationResult(true, "OK");
    }

    public static ValidationResult invalid(String message) {
        return new ValidationResult(false, Objects.requireNonNull(message));
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
            "valid=" + valid +
            ", message='" + message + '\'' +
            '}';
    }
}
