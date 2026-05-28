package com.nexaworks.rafiq.idempotency.filter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.filter.OncePerRequestFilter;

import com.nexaworks.rafiq.idempotency.storage.IdempotencyStore;
import com.nexaworks.rafiq.idempotency.storage.IdempotentResponse;
import com.nexaworks.rafiq.idempotency.storage.Status;
import com.nexaworks.rafiq.idempotency.wrapper.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class WrapperFilter extends OncePerRequestFilter {
    private final IdempotencyStore idempotencyStore;

    public WrapperFilter(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, responseWrapper);
        } finally {
            String key = (String) request.getAttribute("idempotency_key");
            if (key != null) {
                logger.info("idempotency key: " + key + "g");
                byte[] body = responseWrapper.getContent();
                Map<String, String> headers = new HashMap<>();
                responseWrapper.getHeaderNames()
                        .forEach(name -> headers.put(name, responseWrapper.getHeader(name)));
                idempotencyStore.set(key, new IdempotentResponse(response.getStatus(), body,
                        response.getContentType(), Instant.now(), headers, Status.COMPLETED));
            }

        }
    }
}
