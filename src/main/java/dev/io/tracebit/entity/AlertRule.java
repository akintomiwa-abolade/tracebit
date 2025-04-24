package dev.io.tracebit.entity;

import dev.io.tracebit.dto.MatchField;
import dev.io.tracebit.dto.MatchType;
import dev.io.tracebit.security.AttributeEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "alert_rules")
@Builder
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "startup_id", nullable = false)
    private String startupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "field", nullable = false)
    private MatchField field;

    @Column(name = "pattern", nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String pattern;

    @Column(name = "callback_url", nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String callbackUrl;

    @Column(name = "secret_token")
    private String secretToken;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
