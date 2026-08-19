package com.crm.billing.config;

import com.crm.billing.security.TrustedHeadersFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TrustedHeadersFilter trustedHeadersFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(TrustedHeadersFilter trustedHeadersFilter, ObjectMapper objectMapper) {
        this.trustedHeadersFilter = trustedHeadersFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/billing/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/billing/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/billing/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/billing/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/billing/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write(objectMapper.writeValueAsString(
                                    Map.of("status", 401, "error", "Unauthorized",
                                            "message", "Authentication required")));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write(objectMapper.writeValueAsString(
                                    Map.of("status", 403, "error", "Forbidden",
                                            "message", "Insufficient permissions")));
                        }))
                .addFilterBefore(trustedHeadersFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
