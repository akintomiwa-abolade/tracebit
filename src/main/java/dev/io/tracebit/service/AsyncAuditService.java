package dev.io.tracebit.service;

import dev.io.tracebit.entity.AuditLog;
import dev.io.tracebit.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class AsyncAuditService {

    private final AuditLogRepository auditLogRepository;
    private final AlertRuleService alertRuleService;

    public AsyncAuditService(AuditLogRepository auditLogRepository, AlertRuleService alertRuleService) {
        this.auditLogRepository = auditLogRepository;
        this.alertRuleService = alertRuleService;
    }

    @Async("taskExecutor")
    @Transactional
    public void saveAuditLog(AuditLog auditLog) {
        try {
            log.debug("Async saving audit log for user: {}, action: {}", auditLog.getUserId(), auditLog.getAction());
            AuditLog savedLog = auditLogRepository.save(auditLog);

            processAuditLogForAlerts(savedLog);
        } catch (Exception e) {
            log.error("Error saving audit log asynchronously", e);
        }
    }

    private void processAuditLogForAlerts(AuditLog auditLog) {
        try {
            log.debug("Processing audit log for alerts, log ID: {}", auditLog.getId());

            // Delegate to AlertRuleService to process the log for alerts
            alertRuleService.processAuditLogForAlerts(auditLog);

            log.debug("Finished processing audit log for alerts, log ID: {}", auditLog.getId());
        } catch (Exception e) {
            log.error("Error processing audit log for alerts", e);
        }
    }



    @Async("taskExecutor")
    @Transactional
    public void purgeOldAuditLogs(int retentionDays) {
        try {
            log.info("Starting purge of audit logs older than {} days", retentionDays);
            long startTime = System.currentTimeMillis();

            LocalDateTime cutoffDate = LocalDateTime.now().minus(retentionDays, ChronoUnit.DAYS);

            log.info("Would delete audit logs older than: {}", cutoffDate);

            long endTime = System.currentTimeMillis();
            log.info("Audit log purge completed, took: {}ms", (endTime - startTime));
        } catch (Exception e) {
            log.error("Error purging old audit logs", e);
        }
    }
}
