package com.stockleague.backend.global.handler;

import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("[WebSocket] 클라이언트 WebSocket CONNECT 요청 수신");
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null) {
                log.warn("[WebSocket] Authorization 헤더가 없습니다.");
                throw new GlobalException(GlobalErrorCode.INVALID_ACCESS_TOKEN);
            }

            if (!authHeader.startsWith("Bearer ")) {
                log.warn("[WebSocket] Authorization 형식이 올바르지 않습니다. value={}", authHeader);
                throw new GlobalException(GlobalErrorCode.INVALID_ACCESS_TOKEN);
            }

            String token = authHeader.substring(7);
            if (!jwtProvider.validateToken(token)) {
                log.warn("[WebSocket] accessToken 유효성 검사 실패");
                throw new GlobalException(GlobalErrorCode.INVALID_ACCESS_TOKEN);
            }

            Long userId = jwtProvider.getUserId(token);
            accessor.setUser(new StompPrincipal(userId.toString()));

            log.info("[WebSocket] WebSocket 인증 성공 - userId: {}", userId);
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
