package com.crm.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final int readLimit;
    private final int writeLimit;
    private final long windowMs;

    private final Map<String, SlidingWindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${app.ratelimit.read-limit:100}") int readLimit,
            @Value("${app.ratelimit.write-limit:20}") int writeLimit,
            @Value("${app.ratelimit.window-ms:60000}") long windowMs) {
        this.readLimit = readLimit;
        this.writeLimit = writeLimit;
        this.windowMs = windowMs;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String userId = resolveUserId(request);
        String method = request.getMethod() != null ? request.getMethod().name() : "GET";

        boolean isWrite = "POST".equals(method) || "PUT".equals(method)
                || "PATCH".equals(method) || "DELETE".equals(method);

        int limit = isWrite ? writeLimit : readLimit;
        String counterKey = userId + ":" + (isWrite ? "W" : "R");

        SlidingWindowCounter counter = counters.computeIfAbsent(counterKey,
                k -> new SlidingWindowCounter(windowMs));

        if (!counter.tryAcquire(limit)) {
            log.warn("Rate limit exceeded for user {} ({}), method={}", userId, isWrite ? "WRITE" : "READ", method);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().set("Retry-After", String.valueOf(windowMs / 1000));
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String body = "{\"status\":429,\"error\":\"Too Many Requests\","
                    + "\"message\":\"Rate limit exceeded. Try again later.\"}";
            byte[] bytes = body.getBytes();
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        }

        return chain.filter(exchange);
    }

    private String resolveUserId(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            return userId;
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "anonymous";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private static class SlidingWindowCounter {
        private final long windowMs;
        private volatile long windowStart;
        private final AtomicInteger count = new AtomicInteger(0);

        SlidingWindowCounter(long windowMs) {
            this.windowMs = windowMs;
            this.windowStart = System.currentTimeMillis();
        }

        boolean tryAcquire(int limit) {
            long now = System.currentTimeMillis();
            if (now - windowStart >= windowMs) {
                synchronized (this) {
                    if (now - windowStart >= windowMs) {
                        windowStart = now;
                        count.set(0);
                    }
                }
            }
            return count.incrementAndGet() <= limit;
        }
    }
}
