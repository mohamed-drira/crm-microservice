package com.crm.gateway.filter;

import com.crm.gateway.messaging.AuditEvent;
import com.crm.gateway.messaging.AuditEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class AuditLogFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuditLogFilter.class);

    private final AuditEventProducer auditEventProducer;

    public AuditLogFilter(AuditEventProducer auditEventProducer) {
        this.auditEventProducer = auditEventProducer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        String userId = request.getHeaders().getFirst("X-User-Id");
        String username = request.getHeaders().getFirst("X-Username");
        String roles = request.getHeaders().getFirst("X-User-Roles");
        String clientIp = resolveClientIp(request);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            ServerHttpResponse response = exchange.getResponse();
            int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;

            AuditEvent event = AuditEvent.of(
                    request.getMethod() != null ? request.getMethod().name() : "UNKNOWN",
                    request.getURI().getPath(),
                    userId != null ? userId : "anonymous",
                    username != null ? username : "anonymous",
                    roles != null ? roles : "",
                    statusCode,
                    duration,
                    clientIp
            );

            log.debug("AUDIT: {} {} {} {} {}ms {}",
                    event.method(), event.path(), event.username(),
                    event.statusCode(), event.durationMs(), event.clientIp());

            auditEventProducer.publish(event);
        }));
    }

    private String resolveClientIp(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
