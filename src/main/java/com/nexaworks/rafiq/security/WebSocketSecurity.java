package com.nexaworks.rafiq.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurity {

    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager() {
        return MessageMatcherDelegatingAuthorizationManager.builder()
                .simpTypeMatchers(SimpMessageType.CONNECT).authenticated()
                .simpSubscribeDestMatchers("/topic/consultation").authenticated()
                .simpSubscribeDestMatchers("/topic/test").hasRole("DOCTOR")
                .simpSubscribeDestMatchers("/topic/test", "/queue/**", "/user/**").authenticated()
                .simpDestMatchers("/app/**").authenticated().anyMessage().denyAll().build();
    }

    @Bean
    ChannelInterceptor csrfChannelInterceptor() {
        return new ChannelInterceptor() {
        };
    }
}
