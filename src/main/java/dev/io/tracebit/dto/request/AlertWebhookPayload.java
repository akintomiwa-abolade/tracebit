package dev.io.tracebit.dto.request;

import dev.io.tracebit.dto.MatchField;
import dev.io.tracebit.dto.MatchType;
import jdk.jfr.BooleanFlag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlertWebhookPayload {
    private Long ruleId;
    private String ruleName;
    private MatchField matchField;
    private MatchType matchType;
    private Long auditLogId;
    private String userId;
    private String action;
    private String target;
    private LocalDateTime timestamp;
}
