package dev.io.tracebit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
@Configuration
public class OpenApiConfig {

    @Value("${tracebit.api.version:1.0.0}")
    private String apiVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tracebit Audit Logging API")
                        .version(apiVersion)
                        .description("A secure, scalable audit logging API for applications and services. " +
                                "Track user actions, system events, and security incidents with encrypted storage " +
                                "and flexible querying capabilities.")
                        .termsOfService("#")
                        .contact(new Contact()
                                .name("Tracebit Support")
                                .url("#")
                                .email("akintomiwa.abolade@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(Arrays.asList(
                        new Server().url("/").description("Current server"),
                        new Server().url("https://72cc-154-61-166-13.ngrok-free.app").description("Production server"),
                        new Server().url("https://staging-api.tracebit.io").description("Staging server")))
                .components(new Components()
                        .addSecuritySchemes("tracebit-key",
                                new SecurityScheme()
                                        .name("X-TRACEBIT-KEY")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("API key for authentication. Prefix with 'Bearer ' for token-based auth.")));
    }
}
