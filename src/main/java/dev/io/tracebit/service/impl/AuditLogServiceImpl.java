package dev.io.tracebit.service.impl;

import dev.io.tracebit.dto.request.AuditLogRequest;
import dev.io.tracebit.dto.response.ApiResponse;
import dev.io.tracebit.entity.AuditLog;
import dev.io.tracebit.entity.MetaData;
import dev.io.tracebit.repository.AuditLogRepository;
import dev.io.tracebit.service.AsyncAuditService;
import dev.io.tracebit.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static dev.io.tracebit.util.ValidatorUtil.*;

@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AsyncAuditService asyncAuditService;

    @Value("${tracebit.data.retention-days:90}")
    private int retentionDays;

    public AuditLogServiceImpl(
            AuditLogRepository auditLogRepository, 
            AsyncAuditService asyncAuditService) {
        this.auditLogRepository = auditLogRepository;
        this.asyncAuditService = asyncAuditService;
    }

    /**
     * Scheduled task to purge old audit logs
     * Runs at 2:00 AM every day
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledPurgeOldLogs() {
        log.info("Running scheduled purge of old audit logs");
        asyncAuditService.purgeOldAuditLogs(retentionDays);
    }

    @Override
    public ApiResponse createAuditLog(AuditLogRequest request) {
        log.debug("Creating audit log for user: {}, action: {}", request.getUserId(), request.getAction());

        try {
            validateUserId(request.getUserId());
            validateIp(request.getMeta().getIp());
            validateDevice(request.getMeta().getDevice());

            // Create audit log entity
            AuditLog auditLog = AuditLog.builder()
                    .userId(request.getUserId())
                    .action(request.getAction())
                    .target(request.getTarget())
                    .meta(MetaData.builder()
                            .ip(request.getMeta().getIp())
                            .device(request.getMeta().getDevice())
                            .location(request.getMeta().getLocation())
                            .build())
                    .createdAt(LocalDateTime.now())
                    .build();

            // Delegate the actual saving to AsyncAuditService
            asyncAuditService.saveAuditLog(auditLog);

            return ApiResponse.builder()
                    .error(false)
                    .message("Audit log acknowledged successfully.")
                    .build();
        } catch (Exception e) {
            log.error("Error creating audit log", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse searchAuditLogs(String userId, String action, LocalDateTime from, LocalDateTime to, Integer page, Integer size) {
        long startTime = System.currentTimeMillis();
        log.debug("Searching audit logs with filters - userId: {}, action: {}, from: {}, to: {}, page: {}, size: {}", 
                userId, action, from, to, page, size);

        try {
            userId = userId != null ? userId : "";
            action = action != null ? action : "";
            from = from != null ? from : LocalDateTime.MIN;
            to = to != null ? to : LocalDateTime.now();

            Pageable pageable = PageRequest.of(
                page != null ? page : 0, 
                size != null ? size : 20, 
                Sort.by("createdAt").descending()
            );

            Page<AuditLog> auditLogsPage = auditLogRepository.searchAuditLogs(
                    userId, action, from, to, pageable
            );

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("page", auditLogsPage.getNumber());
            metadata.put("size", auditLogsPage.getSize());
            metadata.put("totalElements", auditLogsPage.getTotalElements());
            metadata.put("totalPages", auditLogsPage.getTotalPages());
            metadata.put("first", auditLogsPage.isFirst());
            metadata.put("last", auditLogsPage.isLast());

            Map<String, Object> response = new HashMap<>();
            response.put("logs", auditLogsPage.getContent());
            response.put("pagination", metadata);


            return ApiResponse.builder()
                    .error(false)
                    .message("Audit logs retrieved successfully")
                    .data(response)
                    .build();
        } catch (Exception e) {
            log.error("Error searching audit logs", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "auditLogCache", key = "#id", unless = "#result.error")
    public ApiResponse getAuditLogById(Long id) {
        log.debug("Retrieving audit log with ID: {} (not from cache)", id);

        Optional<AuditLog> auditLogOptional = auditLogRepository.findById(id);

        if (auditLogOptional.isPresent()) {
            log.debug("Audit log found with ID: {}", id);
            return ApiResponse.builder()
                    .error(false)
                    .message("Audit log retrieved successfully")
                    .data(auditLogOptional.get())
                    .build();
        } else {
            log.warn("Audit log not found with ID: {}", id);
            return ApiResponse.builder()
                    .error(true)
                    .message("Audit log not found with ID: " + id)
                    .build();
        }
    }
}
