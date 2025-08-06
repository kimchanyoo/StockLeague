package com.stockleague.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import static org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager.*;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    public AuthorizationManager<Message<?>> messageSecurity() {
        Builder builder = MessageMatcherDelegatingAuthorizationManager.builder();

        builder
                .simpSubscribeDestMatchers("/user/queue/**").permitAll()
                .simpSubscribeDestMatchers("/topic/**").permitAll()
                .simpDestMatchers("/pub/**").authenticated()
                .anyMessage().denyAll();

        return builder.build();
    }
}



