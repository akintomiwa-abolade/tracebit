package dev.io.tracebit.exception;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends BusinessException {
    
    private final Map<String, String> errors;
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.errors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message, "VALIDATION_ERROR");
        this.errors = errors;
    }

    public ValidationException addError(String field, String errorMessage) {
        this.errors.put(field, errorMessage);
        return this;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}