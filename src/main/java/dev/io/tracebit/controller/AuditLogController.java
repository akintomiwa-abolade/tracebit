package dev.io.tracebit.controller;

import dev.io.tracebit.dto.request.AuditLogRequest;
import dev.io.tracebit.dto.response.ApiResponse;
import dev.io.tracebit.dto.response.ProblemDetails;
import dev.io.tracebit.service.AuditLogExportService;
import dev.io.tracebit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/v1/logs")
@SecurityRequirement(name = "tracebit-key")
@Tag(name = "Audit Logs", description = "API for managing audit logs")
@Validated
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogExportService auditLogExportService;

    public AuditLogController(AuditLogService auditLogService, AuditLogExportService auditLogExportService) {
        this.auditLogService = auditLogService;
        this.auditLogExportService = auditLogExportService;
    }

    @Operation(
        summary = "Submit audit log", 
        description = "Creates a new audit log entry with the provided information"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Audit log created successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
             responseCode = "400",
             description = "Bad request",
             content = @Content(schema = @Schema(implementation = ProblemDetails.class))
        )

    })
    @PostMapping
    public ResponseEntity<ApiResponse> saveLog(
            @Valid @RequestBody AuditLogRequest request
    ) {
        return ResponseEntity.ok(auditLogService.createAuditLog(request));
    }

    @Operation(
        summary = "Search audit logs",
        description = "Retrieves audit logs with optional filtering by user ID, action, and date range"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved audit logs",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse> searchLogs(
            @Parameter(description = "Filter by user ID (partial match supported)")
            @RequestParam(required = false) String userId,

            @Parameter(description = "Filter by action (partial match supported)")
            @RequestParam(required = false) String action,

            @Parameter(description = "Filter logs from this date/time (ISO format)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(description = "Filter logs until this date/time (ISO format)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) Integer page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {
        return ResponseEntity.ok(auditLogService.searchAuditLogs(userId, action, from, to, page, size));
    }

    @Operation(
        summary = "Get audit log by ID",
        description = "Retrieves a specific audit log by its ID"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the audit log",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Audit log not found",
            content = @Content(schema = @Schema(implementation = ProblemDetails.class))
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getLogById(
            @Parameter(description = "Audit log ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(auditLogService.getAuditLogById(id));
    }

    @Operation(
            summary = "Export audit logs",
            description = "Exports audit logs in the specified format (CSV or PDF) with optional filtering"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit logs exported successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid export format specified",
                    content = @Content(schema = @Schema(implementation = ProblemDetails.class))
            )
    })
    @GetMapping("/export")
    public void exportLogs(
            @RequestParam(defaultValue = "csv", required = false) String format,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            HttpServletResponse response
    ) throws IOException {
        switch (format.toLowerCase()) {
            case "csv":
                auditLogExportService.exportToCsv(userId, action, from, to, response);
                break;
            case "pdf":
                auditLogExportService.exportToPdf(userId, action, from, to, response);
                break;
            default:
                throw new IllegalArgumentException("Invalid export format: " + format);
        }
    }
}
