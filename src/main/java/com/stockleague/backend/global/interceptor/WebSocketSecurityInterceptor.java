package com.stockleague.backend.global.interceptor;

import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.TokenRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final TokenRedisService redisService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        accessor.setLeaveMutable(true); // 여전히 필요

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("[WebSocket] 클라이언트 WebSocket CONNECT 요청 수신");

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new GlobalException(GlobalErrorCode.INVALID_ACCESS_TOKEN);
            }

            String token = authHeader.substring(7);
            if (!jwtProvider.validateToken(token) || redisService.isBlacklisted(token)) {
                throw new GlobalException(GlobalErrorCode.INVALID_ACCESS_TOKEN);
            }

            Long userId = jwtProvider.getUserId(token);
            Principal principal = new StompPrincipal(String.valueOf(userId));

            accessor.setUser(principal);
            log.info("[WebSocket] WebSocket 인증 성공 - userId: {}", userId);

            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        return message;
    }

    public static class StompPrincipal implements Principal {
        private final String name;

        public StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
