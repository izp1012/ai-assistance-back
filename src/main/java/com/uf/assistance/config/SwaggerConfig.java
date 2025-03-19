package com.uf.assistance.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
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
                        new Server().url("http://localhost:8081").description("Local Server"),  // 로컬 서버 설정
                        new Server().url("https://www.uf-production.com").description("Production Server")  // 프로덕션 서버 설정
                ));
    }
}