package com.nexaworks.rafiq.security;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
@Profile("prod")
public class RateLimitingFilter implements Filter {
    private final ConcurrentMap<String, Bucket> bucketMap = new ConcurrentHashMap<>();

    private Bucket getBucket(String clientIp) {

        Bandwidth limit = Bandwidth.builder().capacity(20).refillGreedy(1, Duration.ofSeconds(3))
                .build();
        return bucketMap.computeIfAbsent(clientIp, k -> Bucket.builder().addLimit(limit).build());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String clientIp = request.getRemoteAddr();
        Bucket bucket = getBucket(clientIp);
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Rate limit exceeded. Try again later\"}");
            response.flushBuffer();
        }
    }
}
