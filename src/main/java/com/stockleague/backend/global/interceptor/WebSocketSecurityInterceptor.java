package com.stockleague.backend.global.interceptor;

import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.infra.redis.TokenRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
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
        accessor.setLeaveMutable(true);

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        try {
            if (StompCommand.CONNECT.equals(command)) {
                log.debug("[WS] CONNECT 수신");

                String authHeader = firstNonNull(
                        accessor.getFirstNativeHeader("Authorization"),
                        accessor.getFirstNativeHeader("authorization"),
                        accessor.getFirstNativeHeader("accessToken")
                );

                if (authHeader == null || authHeader.isBlank()) {
                    log.debug("[WS] CONNECT: Authorization 헤더 없음 → 익명 허용(정책에 따라 막아도 됨)");
                } else {
                    final String raw = authHeader.trim();
                    final String token = raw.regionMatches(true, 0, "Bearer ", 0, 7)
                            ? raw.substring(7).trim()
                            : raw;

                    if (!jwtProvider.validateToken(token)) {
                        log.warn("[WS] CONNECT 거절: JWT 검증 실패");
                        throw new org.springframework.messaging.MessagingException("UNAUTHORIZED: invalid token");
                    }
                    if (redisService.isBlacklisted(token)) {
                        log.warn("[WS] CONNECT 거절: 블랙리스트 토큰");
                        throw new org.springframework.messaging.MessagingException("UNAUTHORIZED: blacklisted token");
                    }

                    Authentication auth = jwtProvider.getAuthentication(token);
                    accessor.setUser(auth);
                    var attrs = accessor.getSessionAttributes();
                    if (attrs != null) {
                        attrs.put("user", auth);
                    }
                    log.info("[WS] CONNECT 인증 성공 - principal={}, heartbeat={}",
                            auth.getName(), accessor.getHeartbeat());
                }
            }
            else if (StompCommand.DISCONNECT.equals(command)) {
                log.debug("[WS] DISCONNECT 처리");
            }
            else {
                if (accessor.getUser() == null) {
                    var attrs = accessor.getSessionAttributes();
                    if (attrs != null) {
                        Object user = attrs.get("user");
                        if (user instanceof Principal principal) {
                            accessor.setUser(principal);
                        }
                    }
                }
            }

            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());

        } catch (org.springframework.messaging.MessagingException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[WS] preSend 예외: {}", e.toString());
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }
    }

    private static String firstNonNull(String... values) {
        return java.util.stream.Stream.of(values)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}