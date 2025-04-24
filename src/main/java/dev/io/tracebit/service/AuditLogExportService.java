package dev.io.tracebit.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

public interface AuditLogExportService {
    void exportToCsv(String userId, String action, LocalDateTime from, LocalDateTime to, HttpServletResponse response) throws IOException;
    void exportToPdf(String userId, String action, LocalDateTime from, LocalDateTime to, HttpServletResponse response) throws IOException;
}

