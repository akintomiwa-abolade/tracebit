package dev.io.tracebit.service;

import dev.io.tracebit.dto.request.AlertRuleRequest;
import dev.io.tracebit.dto.response.ApiResponse;
import dev.io.tracebit.entity.AuditLog;

public interface AlertRuleService {

    ApiResponse createAlertRule(AlertRuleRequest request);

    ApiResponse updateAlertRule(Long id, AlertRuleRequest request);

    ApiResponse deleteAlertRule(Long id);

    ApiResponse getAlertRuleById(Long id);

    ApiResponse getAlertRulesByStartupId(String startupId);

    void processAuditLogForAlerts(AuditLog auditLog);
}
