package dev.io.tracebit.entity;

import dev.io.tracebit.security.AttributeEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "audit_logs")
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AttributeEncryptor.class)
    private String userId;

    @Convert(converter = AttributeEncryptor.class)
    private String action;

    @Convert(converter = AttributeEncryptor.class)
    private String target;

    @Embedded
    private MetaData meta;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

