package dev.io.tracebit.entity;

import dev.io.tracebit.security.AttributeEncryptor;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@Builder
public class MetaData {
    @Convert(converter = AttributeEncryptor.class)
    private String ip;
    @Convert(converter = AttributeEncryptor.class)
    private String device;
    @Convert(converter = AttributeEncryptor.class)
    private String location;
}

