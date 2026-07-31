package com.nexaworks.rafiq.security;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.config.RateLimitProperties;
import com.nexaworks.rafiq.dto.response.common.ApiErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {
    private static final String KEY_PREFIX = "rate-limit:";

    private final RedisConnectionFactory redisConnectionFactory;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!properties.isEnabled() || !isLimitedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String key = buildKey(request);
            RedisAtomicLong counter = new RedisAtomicLong(key, redisConnectionFactory);
            long currentCount = counter.incrementAndGet();
            if (currentCount == 1) {
                counter.expire(Duration.ofSeconds(properties.getWindowSeconds()));
            }

            long remaining = Math.max(0, properties.getLimit() - currentCount);
            response.setHeader("X-RateLimit-Limit", String.valueOf(properties.getLimit()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            response.setHeader("X-RateLimit-Window-Seconds",
                    String.valueOf(properties.getWindowSeconds()));

            if (currentCount > properties.getLimit()) {
                writeRateLimitResponse(response);
                return;
            }
        } catch (DataAccessException ex) {
            log.warn("Redis rate limiting unavailable; allowing request: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLimitedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return properties.getPaths().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String buildKey(HttpServletRequest request) {
        long window = System.currentTimeMillis() / (properties.getWindowSeconds() * 1000L);
        return KEY_PREFIX + clientId(request) + ":" + request.getMethod() + ":"
                + request.getRequestURI() + ":" + window;
    }

    private String clientId(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(),
                new ApiErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(),
                        "Too many requests. Please try again later.", LocalDateTime.now()));
        response.flushBuffer();
    }
}
