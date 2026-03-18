package com.operimus.Marketing.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

// Dedicated CORS configuration to complement the main SecurityConfig.
// Note: We intentionally do NOT enable WebSecurity or declare a SecurityFilterChain here
// to avoid conflicts with com.operimus.Marketing.security.SecurityConfig.
@Configuration
public class CorsConfig {

    @Value("${machine.url}")
    private String machineUrl;
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000", "http://192.168.160.11:3000", "http://10.0.1.21:3000", "http://192.168.45.87:3000", "http://" + machineUrl + ":3000"));
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
