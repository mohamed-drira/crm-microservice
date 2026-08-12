package com.crm.gateway.security;

import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Runs after Spring Security has authenticated the token and forwards the
 * Keycloak identity to downstream services as X-User-* headers.
 */
@Component
public class JwtHeaderForwardFilter implements WebFilter, Ordered {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_ROLES = "X-User-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .flatMap(auth -> {
                    Jwt jwt = auth.getToken();
                    ServerWebExchange mutated = exchange.mutate()
                            .request(builder -> builder.headers(headers -> {
                                headers.set(HEADER_USER_ID, jwt.getSubject());
                                String username = jwt.getClaimAsString("preferred_username");
                                headers.set(HEADER_USERNAME, username != null ? username : jwt.getSubject());
                                String roles = realmRoles(jwt);
                                if (roles != null) {
                                    headers.set(HEADER_ROLES, roles);
                                }
                            }))
                            .build();
                    return chain.filter(mutated);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private String realmRoles(Jwt jwt) {
        Object claim = jwt.getClaims().get("realm_access");
        if (claim instanceof Map<?, ?> realmAccess
                && realmAccess.get("roles") instanceof Collection<?> roles) {
            String joined = roles.stream().map(Object::toString).collect(Collectors.joining(","));
            return joined.isEmpty() ? null : joined;
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
