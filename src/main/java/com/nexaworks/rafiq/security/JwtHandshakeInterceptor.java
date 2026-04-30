package com.nexaworks.rafiq.security;

import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String JWT_ATTR = "jwt";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            Cookie[] cookies = servletRequest.getServletRequest().getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        attributes.put(JWT_ATTR, cookie.getValue());
                        log.debug("WebSocket handshake captured jwt cookie");
                        return true;
                    }
                }
            }
        }
        log.warn("Rejecting WebSocket handshake — no jwt cookie present");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               @Nullable Exception exception) {
    }
}
