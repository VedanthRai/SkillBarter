package com.skillbarter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * SkillBarter - Time-Banking & Skill Exchange Platform
 *
 * UE23CS352B Object Oriented Analysis & Design Project
 *
 * Architecture:
 *  - Spring Boot MVC (enforced)
 *  - Thymeleaf frontend
 *  - MySQL persistence via JPA/Hibernate
 *  - Design Patterns: Strategy, Observer, Builder, Decorator
 *  - SOLID Principles: SRP, OCP, LSP, DIP applied throughout
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SkillBarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillBarterApplication.class, args);
    }
}
