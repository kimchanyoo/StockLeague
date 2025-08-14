package com.stockleague.backend.global.interceptor;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.infra.redis.TokenRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final TokenRedisService redisService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        accessor.setLeaveMutable(true);

        StompCommand cmd = accessor.getCommand();
        if (cmd == null) return message;

        switch (cmd) {
            case CONNECT -> {
                String raw = firstNonNull(
                        accessor.getFirstNativeHeader("Authorization"),
                        accessor.getFirstNativeHeader("authorization")
                );

                if (raw != null) {
                    String token = normalizeBearer(raw);
                    if (token.isEmpty()) {
                        log.warn("[WS] CONNECT 거절: 빈 토큰");
                        throw new MessagingException("UNAUTHORIZED: empty token");
                    }
                    if (!jwtProvider.validateToken(token)) {
                        log.warn("[WS] CONNECT 거절: 토큰 검증 실패");
                        throw new MessagingException("UNAUTHORIZED: invalid token");
                    }
                    if (redisService.isBlacklisted(token)) {
                        log.warn("[WS] CONNECT 거절: 블랙리스트 토큰");
                        throw new MessagingException("UNAUTHORIZED: blacklisted token");
                    }
                }
            }
            case SUBSCRIBE -> {
                String dest = accessor.getDestination();
                if (dest != null && dest.startsWith("/user/") && accessor.getUser() == null) {
                    log.warn("[WS] SUBSCRIBE 거절: /user/** 구독에 인증 필요");
                    throw new MessagingException("FORBIDDEN: /user/** requires auth");
                }
            }
            case SEND -> {
                String dest = accessor.getDestination();

                if (dest != null && (dest.startsWith("/topic") || dest.startsWith("/user"))) {
                    log.warn("[WS] SEND 거절: 브로커 목적지로 직접 전송 시도 dest={}", dest);
                    throw new MessagingException("FORBIDDEN: cannot SEND to broker destinations");
                }

                if (dest != null && !dest.startsWith("/pub")) {
                    log.warn("[WS] SEND 거절: 허용되지 않은 목적지 dest={}", dest);
                    throw new MessagingException("FORBIDDEN: client must SEND to /pub/**");
                }
            }
            default -> { /* no-op */ }
        }
        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    private static String normalizeBearer(String raw) {
        String v = raw.trim().replace("\"", "");
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return v.substring(7).trim();
        }
        return v;
    }
}
