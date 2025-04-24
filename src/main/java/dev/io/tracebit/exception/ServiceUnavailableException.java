package dev.io.tracebit.exception;

/**
 * Exception thrown when a service is unavailable.
 */
public class ServiceUnavailableException extends BusinessException {
    
    /**
     * Constructs a new service unavailable exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public ServiceUnavailableException(String message) {
        super(message, "SERVICE_UNAVAILABLE");
    }
    
    /**
     * Constructs a new service unavailable exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause, "SERVICE_UNAVAILABLE");
    }
    
    /**
     * Constructs a new service unavailable exception for a specific service.
     * 
     * @param serviceName the name of the service
     */
    public ServiceUnavailableException(String serviceName, String reason) {
        super("Service " + serviceName + " is unavailable: " + reason, "SERVICE_UNAVAILABLE");
    }
}