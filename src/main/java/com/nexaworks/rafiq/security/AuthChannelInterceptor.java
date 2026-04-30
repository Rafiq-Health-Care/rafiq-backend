package com.nexaworks.rafiq.security;

import java.security.Principal;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.service.authentication.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        String jwt = sessionAttributes == null
                ? null
                : (String) sessionAttributes.get(JwtHandshakeInterceptor.JWT_ATTR);

        if (jwt == null) {
            log.warn("STOMP CONNECT rejected — no jwt in session attributes");
            throw new AccessDeniedException("Missing JWT");
        }

        Authentication authentication = jwtService.validate(jwt);
        if (authentication == null) {
            log.warn("STOMP CONNECT rejected — invalid JWT");
            throw new AccessDeniedException("Invalid JWT");
        }

        accessor.setUser(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("STOMP CONNECT authenticated for principal: {}", authentication.getPrincipal());

        return message;
    }
}
