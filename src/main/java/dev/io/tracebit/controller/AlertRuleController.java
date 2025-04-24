package dev.io.tracebit.controller;

import dev.io.tracebit.dto.request.AlertRuleRequest;
import dev.io.tracebit.dto.response.ApiResponse;
import dev.io.tracebit.dto.response.ProblemDetails;
import dev.io.tracebit.service.AlertRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/alert-rules")
@SecurityRequirement(name = "tracebit-key")
@Tag(name = "Alert Rules", description = "API for managing real-time alert rules on audit log events")
@Validated
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    public AlertRuleController(AlertRuleService alertRuleService) {
        this.alertRuleService = alertRuleService;
    }

    @Operation(
            summary = "Create alert rule",
            description = "Creates a new alert rule. Rules can be triggered based on userId, action, or target, " +
                    "with matching logic (EXACT, CONTAINS, REGEX). A callback URL will receive webhook notifications."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Alert rule created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ProblemDetails.class))
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse> createAlertRule(
            @Valid @RequestBody AlertRuleRequest request
    ) {
        return ResponseEntity.ok(alertRuleService.createAlertRule(request));
    }

    @Operation(
            summary = "Update alert rule",
            description = "Updates an existing alert rule. You can change field, pattern, or callback URL."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Alert rule updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Alert rule not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetails.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateAlertRule(
            @Parameter(description = "Alert rule ID") @PathVariable Long id,
            @Valid @RequestBody AlertRuleRequest request
    ) {
        return ResponseEntity.ok(alertRuleService.updateAlertRule(id, request));
    }

    @Operation(
            summary = "Delete alert rule",
            description = "Deletes an alert rule so it no longer triggers on future audit logs."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Alert rule deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Alert rule not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetails.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAlertRule(
            @Parameter(description = "Alert rule ID") @PathVariable Long id
    ) {
        return ResponseEntity.ok(alertRuleService.deleteAlertRule(id));
    }

    @Operation(
            summary = "Get alert rule by ID",
            description = "Retrieves a single alert rule configuration by its unique ID."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the alert rule",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Alert rule not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetails.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getAlertRuleById(
            @Parameter(description = "Alert rule ID") @PathVariable Long id
    ) {
        return ResponseEntity.ok(alertRuleService.getAlertRuleById(id));
    }

    @Operation(
            summary = "Get alert rules by startup ID",
            description = "Returns all active alert rules that belong to a given startup."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved alert rules",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/startup/{startupId}")
    public ResponseEntity<ApiResponse> getAlertRulesByStartupId(
            @Parameter(description = "Startup ID") @PathVariable String startupId
    ) {
        return ResponseEntity.ok(alertRuleService.getAlertRulesByStartupId(startupId));
    }
}
