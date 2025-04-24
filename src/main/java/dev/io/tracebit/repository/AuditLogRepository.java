package dev.io.tracebit.repository;

import dev.io.tracebit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR LOWER(a.userId) LIKE LOWER(CONCAT('%', :userId, '%'))) AND " +
           "(:action IS NULL OR LOWER(a.action) LIKE LOWER(CONCAT('%', :action, '%'))) AND " +
           "a.createdAt BETWEEN :from AND :to")
    Page<AuditLog> searchAuditLogs(
            @Param("userId") String userId,
            @Param("action") String action,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
