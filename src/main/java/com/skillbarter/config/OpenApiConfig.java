package com.skillbarter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * 
 * Access documentation at:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI skillBarterOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://skillbarter.com");
        prodServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setName("SkillBarter Team");
        contact.setEmail("support@skillbarter.com");
        contact.setUrl("https://skillbarter.com");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("SkillBarter API")
                .version("1.0.0")
                .description("Time-Banking and Skill Exchange Platform API\n\n" +
                             "Trade skills without money. Teach what you know, learn what you need.\n\n" +
                             "**Features:**\n" +
                             "- User authentication and profiles\n" +
                             "- Skill listing and search\n" +
                             "- Session booking and management\n" +
                             "- Credit-based escrow system\n" +
                             "- Dispute resolution\n" +
                             "- Gamification (badges, streaks, leaderboard)\n" +
                             "- Real-time notifications\n" +
                             "- AI-powered skill matching\n\n" +
                             "**Authentication:**\n" +
                             "Most endpoints require authentication. Use the /auth/login endpoint to obtain a session.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, prodServer));
    }
}
