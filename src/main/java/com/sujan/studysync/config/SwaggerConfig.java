package com.sujan.studysync.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.backend-url}")
    private String backendUrl;

    @Bean
    public OpenAPI openAPI() {

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development");

        Server productionServer = new Server()
                .url(backendUrl)
                .description("Production");

        return new OpenAPI()

                // ─── Project Info ────────────────────────────────
                .info(new Info()
                        .title("StudySync API")
                        .description("""
                    StudySync — Real-time Study Collaboration Platform
                    
                    ## Authentication
                    Most endpoints require a JWT Bearer token.
                    
                    1. Call POST /api/auth/login to get an access token
                    2. Click "Authorize" and enter: Bearer {your-token}
                    """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sujan Lamichhane")
                                .email("sujan@studysync.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))

                // ─── Servers ─────────────────────────────────────
                .servers(List.of(localServer, productionServer))

                // ─── Security ────────────────────────────────────
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("Bearer Authentication"))

                .components(new Components()
                        .addSecuritySchemes(
                                "Bearer Authentication",

                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Enter JWT token from /api/auth/login")
                        ));
    }
}