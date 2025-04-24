package dev.io.tracebit.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(resourceType + " not found with ID: " + resourceId, "RESOURCE_NOT_FOUND");
    }
}