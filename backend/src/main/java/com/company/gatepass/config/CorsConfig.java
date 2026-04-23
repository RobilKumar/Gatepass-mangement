package com.company.gatepass.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*,http://192.168.*.*,http://10.*.*.*,http://172.16.*.*,http://172.17.*.*,http://172.18.*.*,http://172.19.*.*,http://172.20.*.*,http://172.21.*.*,http://172.22.*.*,http://172.23.*.*,http://172.24.*.*,http://172.25.*.*,http://172.26.*.*,http://172.27.*.*,http://172.28.*.*,http://172.29.*.*,http://172.30.*.*,http://172.31.*.*}")
    private String allowedOriginPatterns;

    // Registers CORS rules so the React frontend can call backend API endpoints.
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            // Applies allowed origins, methods, and headers to every /api route.
            @SuppressWarnings("null")
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns(patterns())
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Content-Disposition")
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }

    // Splits the comma-separated configured origin patterns into an array Spring can use.
    private String[] patterns() {
        return allowedOriginPatterns.split("\\s*,\\s*");
    }
}
