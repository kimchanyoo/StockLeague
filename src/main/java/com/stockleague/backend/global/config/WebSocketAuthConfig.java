package com.stockleague.backend.global.config;

import com.stockleague.backend.auth.jwt.JwtProvider;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;

@Slf4j
@Configuration
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtProvider jwtProvider;

    public WebSocketAuthConfig(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");

                    log.info("[WebSocket CONNECT] 헤더들: {}", accessor.toNativeHeaderMap());

                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7); // "Bearer " 제거

                        if (jwtProvider.validateToken(token)) {
                            Long userId = jwtProvider.getUserId(token);

                            accessor.setUser(new UsernamePasswordAuthenticationToken(
                                    userId.toString(), null, List.of()
                            ));

                            log.info("[WebSocket CONNECT] 인증 성공: userId={}", userId);
                        } else {
                            log.warn("[WebSocket CONNECT] 유효하지 않은 토큰");
                        }
                    } else {
                        log.warn("[WebSocket CONNECT] Authorization 헤더 누락 또는 형식 오류");
                    }
                }

                return message;
            }
        });
    }

}
