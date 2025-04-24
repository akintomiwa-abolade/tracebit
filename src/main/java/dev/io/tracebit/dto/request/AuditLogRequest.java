package dev.io.tracebit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    @NotBlank(message = "Action is required")
    private String action;
    @NotBlank(message = "Target is required")
    private String target;
    @NotNull(message = "Meta object is required")
    private MetaDataRequest meta;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MetaDataRequest {
        @NotBlank(message = "IP address is required")
        private String ip;
        @NotBlank(message = "Device is required")
        private String device;
        private String location;
    }
}

