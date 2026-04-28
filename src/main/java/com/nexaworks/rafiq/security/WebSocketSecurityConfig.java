package com.nexaworks.rafiq.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {

        messages
                .nullDestMatcher().authenticated()
                .simpSubscribeDestMatchers("/user/**").authenticated()
                .simpDestMatchers("/app/**").authenticated()
                .simpDestMatchers("/topic/consultation").authenticated()
                .simpDestMatchers("/topic/public/**").permitAll()
                .simpDestMatchers("/topic/admin/**").hasRole("ADMIN")
                .anyMessage().denyAll();

        return messages.build();
    }
}