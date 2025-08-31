package com.example.backend.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "여담人 API 명세서",
                description = "여행 계획 관리 애플리케이션 API",
                version = "1.0"
        )
)
@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("JWT");

        return new OpenAPI()
                .servers(Arrays.asList(
                        new Server().url("http://yeodam.site").description("도메인 서버"),
                        new Server().url("http://43.200.227.73:8080").description("실제 서버")
                ))
                .addSecurityItem(securityRequirement)
                .components(new Components());
    }
}