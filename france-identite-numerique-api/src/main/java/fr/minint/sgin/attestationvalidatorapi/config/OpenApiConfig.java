package fr.minint.sgin.attestationvalidatorapi.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 *
 * Open API Configuration
 *
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public GroupedOpenApi apiV1() {
        return GroupedOpenApi.builder().group("attestation-validator-api-v1").pathsToMatch("/validation/v1/**").build();
    }

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI().info(new Info().title("Attestation validator API - Spring Boot Swagger Configuration")
                .description("Swagger configuration for microservice attestation-validator-api")
                .version("v1.0.0"));
    }
}