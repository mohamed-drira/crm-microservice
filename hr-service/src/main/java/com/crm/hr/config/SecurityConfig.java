package com.crm.hr.config;

import com.crm.hr.security.TrustedHeadersFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * The gateway is the only entry point and validates every JWT at the edge.
 * This service trusts the forwarded identity headers and leaves authorization
 * decisions to the controllers.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TrustedHeadersFilter trustedHeadersFilter;

    public SecurityConfig(TrustedHeadersFilter trustedHeadersFilter) {
        this.trustedHeadersFilter = trustedHeadersFilter;
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
                        .anyRequest().permitAll())
                .addFilterBefore(trustedHeadersFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
