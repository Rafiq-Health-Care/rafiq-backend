package com.nexaworks.rafiq.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {
    private final ConcurrentMap<String, Bucket> bucketMap = new ConcurrentHashMap<>();
    private Bucket getBucket(String clientIp){

        Bandwidth limit = Bandwidth.builder()
                .capacity(20)
                .refillGreedy(1,Duration.ofSeconds(10))
                .build();
        return bucketMap.computeIfAbsent(clientIp, k -> Bucket.builder().addLimit(limit).build());

    }
    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String clientIp = request.getRemoteAddr();
        Bucket bucket = getBucket(clientIp);
        if (bucket.tryConsume(1)){
            filterChain.doFilter(servletRequest, servletResponse);
        } else {

            servletResponse.getWriter().write("Too many requests - Rate limit exceeded");
            servletResponse.getWriter().flush();
        }

    }
}
