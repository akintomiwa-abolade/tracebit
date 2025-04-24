package dev.io.tracebit.exception;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }

    public UnauthorizedException() {
        super("Unauthorized access", "UNAUTHORIZED");
    }

    public UnauthorizedException(String resourceType, Object resourceId) {
        super("Unauthorized access to " + resourceType + " with ID: " + resourceId, "UNAUTHORIZED");
    }
}