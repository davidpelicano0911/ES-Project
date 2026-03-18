package com.operimus.Marketing.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@Profile("!test")
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Allow Swagger UI and OpenAPI docs
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()

                // Allow Keycloak redirect URL
                .requestMatchers("/login/oauth2/**").permitAll()

                // Allow health checks
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                // Allow preflight CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Role-protected endpoints
                .requestMatchers("/api/admin/**").hasRole("MARKETING_MANAGER")
                
                
                .requestMatchers("/public/**").permitAll()
                // Public API endpoints - no authentication required
                .requestMatchers(new AntPathRequestMatcher("/api/v*/form-template/public/*", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v*/landing-pages/public/*", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v*/form-submissions/public", "POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v*/events", "POST")).permitAll()

                // Everything else needs JWT
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());

        return http.build();
    }
}
