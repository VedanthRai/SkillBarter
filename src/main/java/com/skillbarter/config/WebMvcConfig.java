package com.skillbarter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;
// Configures MVC settings like view resolvers and interceptors
/**
 * Web MVC Configuration.
 *
 * Registers:
 *  1. LocalDateTime formatter for HTML datetime-local inputs (yyyy-MM-dd'T'HH:mm)
 *  2. Static resource handler for uploaded files (certificates, avatars)
 *
 * SOLID – SRP: all web layer config in one place.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        registrar.registerFormatters(registry);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded certificates and avatars from the filesystem
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");

        // Serve generated PDF receipts
        registry.addResourceHandler("/receipts/**")
                .addResourceLocations("file:./receipts/");
    }
}
