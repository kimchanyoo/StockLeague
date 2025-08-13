package com.stockleague.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

import static org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager.*;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager() {
        Builder builder = MessageMatcherDelegatingAuthorizationManager.builder();

        return builder
                .nullDestMatcher().permitAll()

                .simpTypeMatchers(
                        SimpMessageType.CONNECT,
                        SimpMessageType.CONNECT_ACK,
                        SimpMessageType.HEARTBEAT,
                        SimpMessageType.UNSUBSCRIBE,
                        SimpMessageType.DISCONNECT
                ).permitAll()

                .simpDestMatchers("/pub/**").authenticated()

                .simpSubscribeDestMatchers("/topic/**").permitAll()

                .simpSubscribeDestMatchers("/user/**").authenticated()

                .anyMessage().denyAll()
                .build();
    }
}
