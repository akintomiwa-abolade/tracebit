package dev.io.tracebit.service;

import dev.io.tracebit.dto.request.AuditLogRequest;
import dev.io.tracebit.dto.response.ApiResponse;
import dev.io.tracebit.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {
    ApiResponse createAuditLog(AuditLogRequest auditLogRequest);
    ApiResponse searchAuditLogs(String userId, String action, LocalDateTime from, LocalDateTime to, Integer page, Integer size);
    ApiResponse getAuditLogById(Long id);
}
