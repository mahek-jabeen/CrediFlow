package com.loanmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class for global CORS settings.
 * Enables cross-origin requests from the frontend application.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure CORS mappings to allow frontend access to backend APIs.
     * Maps to /api/** to handle all API endpoints including preflight OPTIONS requests.
     * 
     * @param registry the CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
