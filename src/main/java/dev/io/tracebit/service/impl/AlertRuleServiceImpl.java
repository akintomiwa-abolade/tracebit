package dev.io.tracebit.service.impl;

import dev.io.tracebit.dto.MatchType;
import dev.io.tracebit.dto.request.AlertRuleRequest;
import dev.io.tracebit.dto.request.AlertWebhookPayload;
import dev.io.tracebit.dto.response.ApiResponse;
import dev.io.tracebit.entity.AlertRule;
import dev.io.tracebit.entity.AuditLog;
import dev.io.tracebit.exception.ValidationException;
import dev.io.tracebit.repository.AlertRuleRepository;
import dev.io.tracebit.service.AlertRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Slf4j
@Service
public class AlertRuleServiceImpl implements AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;
    private final RestTemplate restTemplate;

    public AlertRuleServiceImpl(AlertRuleRepository alertRuleRepository) {
        this.alertRuleRepository = alertRuleRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    @Transactional
    public ApiResponse createAlertRule(AlertRuleRequest request) {
        try {
            log.debug("Creating alert rule: {}", request.getName());

            if (request.getMatchType() == MatchType.REGEX) {
                try {
                    Pattern.compile(request.getPattern());
                } catch (PatternSyntaxException e) {
                    throw new ValidationException("Invalid regex pattern", Map.of("pattern", "Malformed regex pattern"));
                }
            }

            AlertRule alertRule = AlertRule.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .startupId(request.getStartupId())
                    .matchType(request.getMatchType())
                    .field(request.getField())
                    .pattern(request.getPattern())
                    .callbackUrl(request.getCallbackUrl())
                    .secretToken(request.getSecretToken())
                    .active(request.getActive())
                    .createdAt(LocalDateTime.now())
                    .build();

            AlertRule savedRule = alertRuleRepository.save(alertRule);

            log.info("Alert rule created with ID: {}", savedRule.getId());

            return ApiResponse.builder()
                    .error(false)
                    .message("Alert rule created successfully")
                    .data(savedRule)
                    .build();

        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            log.error("Error creating alert rule", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ApiResponse updateAlertRule(Long id, AlertRuleRequest request) {
        try {
            log.debug("Updating alert rule with ID: {}", id);

            Optional<AlertRule> existingRuleOpt = alertRuleRepository.findById(id);

            if (existingRuleOpt.isEmpty()) {
                log.warn("Alert rule not found with ID: {}", id);
                return ApiResponse.builder()
                        .error(true)
                        .message("Alert rule not found with ID: " + id)
                        .build();
            }

            if (request.getMatchType() == MatchType.REGEX) {
                try {
                    Pattern.compile(request.getPattern());
                } catch (PatternSyntaxException e) {
                    throw new ValidationException("Invalid regex pattern", Map.of("pattern", "Malformed regex pattern"));
                }
            }

            AlertRule rule = existingRuleOpt.get();
            rule.setName(request.getName());
            rule.setDescription(request.getDescription());
            rule.setStartupId(request.getStartupId());
            rule.setMatchType(request.getMatchType());
            rule.setField(request.getField());
            rule.setPattern(request.getPattern());
            rule.setCallbackUrl(request.getCallbackUrl());
            rule.setSecretToken(request.getSecretToken());
            rule.setActive(request.getActive());
            rule.setUpdatedAt(LocalDateTime.now());

            AlertRule updated = alertRuleRepository.save(rule);

            log.info("Alert rule updated with ID: {}", updated.getId());

            return ApiResponse.builder()
                    .error(false)
                    .message("Alert rule updated successfully")
                    .data(updated)
                    .build();

        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            log.error("Error updating alert rule", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ApiResponse deleteAlertRule(Long id) {
        try {
            log.debug("Deleting alert rule with ID: {}", id);

            Optional<AlertRule> existingRuleOpt = alertRuleRepository.findById(id);

            if (existingRuleOpt.isPresent()) {
                alertRuleRepository.deleteById(id);

                log.info("Alert rule deleted with ID: {}", id);

                return ApiResponse.builder()
                        .error(false)
                        .message("Alert rule deleted successfully")
                        .build();
            } else {
                log.warn("Alert rule not found with ID: {}", id);

                return ApiResponse.builder()
                        .error(true)
                        .message("Alert rule not found with ID: " + id)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error deleting alert rule", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getAlertRuleById(Long id) {
        try {
            log.debug("Getting alert rule with ID: {}", id);

            Optional<AlertRule> alertRuleOpt = alertRuleRepository.findById(id);

            if (alertRuleOpt.isPresent()) {
                log.debug("Alert rule found with ID: {}", id);

                return ApiResponse.builder()
                        .error(false)
                        .message("Alert rule retrieved successfully")
                        .data(alertRuleOpt.get())
                        .build();
            } else {
                log.warn("Alert rule not found with ID: {}", id);

                return ApiResponse.builder()
                        .error(true)
                        .message("Alert rule not found with ID: " + id)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error getting alert rule", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getAlertRulesByStartupId(String startupId) {
        try {
            log.debug("Getting alert rules for startup ID: {}", startupId);

            List<AlertRule> alertRules = alertRuleRepository.findByStartupIdAndActiveTrue(startupId);

            log.debug("Found {} alert rules for startup ID: {}", alertRules.size(), startupId);

            return ApiResponse.builder()
                    .error(false)
                    .message("Alert rules retrieved successfully")
                    .data(alertRules)
                    .build();
        } catch (Exception e) {
            log.error("Error getting alert rules for startup", e);
            throw e;
        }
    }

    @Override
    @Async("taskExecutor")
    public void processAuditLogForAlerts(AuditLog auditLog) {
        try {
            log.debug("Processing audit log for alerts, log ID: {}", auditLog.getId());

            List<AlertRule> allRules = alertRuleRepository.findAllByActiveTrue();

            for (AlertRule rule : allRules) {
                String valueToCompare = switch (rule.getField()) {
                    case USER_ID -> auditLog.getUserId();
                    case ACTION -> auditLog.getAction();
                    case TARGET -> auditLog.getTarget();
                };

                if (matches(rule.getMatchType(), rule.getPattern(), valueToCompare)) {
                    sendWebhookNotification(rule, auditLog);
                }
            }
        } catch (Exception e) {
            log.error("Error processing audit log for alerts", e);
        }
    }

    private void sendWebhookNotification(AlertRule rule, AuditLog auditLog) {
        try {
            log.debug("Sending webhook notification for rule ID: {} to URL: {}",
                    rule.getId(), rule.getCallbackUrl());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (rule.getSecretToken() != null && !rule.getSecretToken().isBlank()) {
                headers.set("X-TRACEBIT-SIGNATURE", rule.getSecretToken());
            }

            AlertWebhookPayload payload = AlertWebhookPayload.builder()
                    .ruleId(rule.getId())
                    .ruleName(rule.getName())
                    .matchField(rule.getField())
                    .matchType(rule.getMatchType())
                    .auditLogId(auditLog.getId())
                    .userId(auditLog.getUserId())
                    .action(auditLog.getAction())
                    .target(auditLog.getTarget())
                    .timestamp(auditLog.getCreatedAt())
                    .build();

            HttpEntity<AlertWebhookPayload> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(rule.getCallbackUrl(), request, String.class);

            log.info("Webhook notification sent successfully for rule ID: {}", rule.getId());
        } catch (Exception e) {
            log.error("Error sending webhook notification for rule ID: {}", rule.getId(), e);
        }
    }

    private boolean matches(MatchType matchType, String pattern, String input) {
        if (input == null || pattern == null) return false;

        return switch (matchType) {
            case EXACT -> input.equalsIgnoreCase(pattern);
            case CONTAINS -> input.toLowerCase().contains(pattern.toLowerCase());
            case REGEX -> input.matches(pattern);
        };
    }
}
