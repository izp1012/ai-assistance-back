package com.uf.assistance.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition
public class SwaggerConfig {

    @Value("${app.server.dev-url}")
    private String devServerUrl;

    @Value("${app.server.local-url}")
    private String localServerUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Security Requirement 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .schemaRequirement("BearerAuth", securityScheme)
                .info(new Info()
                        .title("AI Subscription API")
                        .version("1.0")
                        .description("사용자가 AI를 구독하고, 채팅을 할 수 있는 API입니다.")
                        .contact(new Contact()
                                .name("개발팀")
                                .email("uf-support@google.com"))
                )

                // 추가 설정 속성
                // API가 배포된 서버들을 정의
                .servers(List.of(
                        new Server().url(devServerUrl).description("Develop Server"),
                        new Server().url(localServerUrl).description("Local Server"),
                        new Server().url("https://www.uf-production.com").description("Production Server")
                ));
    }
}