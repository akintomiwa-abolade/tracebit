package dev.io.tracebit.dto.request;

import dev.io.tracebit.dto.MatchField;
import dev.io.tracebit.dto.MatchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Startup ID is required")
    private String startupId;

    @NotNull(message = "Match type is required")
    private MatchType matchType;

    @NotNull(message = "Match field is required")
    private MatchField field;

    @NotBlank(message = "Pattern is required")
    private String pattern;

    @NotBlank(message = "Callback URL is required")
    private String callbackUrl;

    private String secretToken;

    @NotNull(message = "Active status is required")
    private Boolean active;
}