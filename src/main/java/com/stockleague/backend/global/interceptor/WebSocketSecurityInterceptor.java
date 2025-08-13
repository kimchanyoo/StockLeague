package com.stockleague.backend.global.interceptor;

import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.global.security.StompPrincipal;
import com.stockleague.backend.infra.redis.TokenRedisService;
import java.util.Arrays;
import java.util.Map;
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

        try {
            if (StompCommand.CONNECT.equals(cmd)) {
                Map<String, ?> headers = accessor.toNativeHeaderMap();
                log.info("[WS] CONNECT headers={}", headers);

                String authHeader = firstNonNull(
                        accessor.getFirstNativeHeader("Authorization"),
                        accessor.getFirstNativeHeader("authorization"),
                        accessor.getFirstNativeHeader("accessToken")
                );

                if (authHeader == null || authHeader.isBlank()) {
                    // 정책에 따라 익명 허용/거부를 선택
                    log.warn("[WS] CONNECT: Authorization 없음 → 익명 허용(임시)");
                } else {
                    String raw = authHeader.trim().replace("\"", "");
                    String token = raw.regionMatches(true, 0, "Bearer ", 0, 7)
                            ? raw.substring(7).trim()
                            : raw;

                    if (!jwtProvider.validateToken(token)) {
                        String prefix = safePrefix(token);
                        log.warn("[WS] CONNECT 거절: JWT 검증 실패 tokenPrefix={}", prefix);
                        throw new MessagingException("UNAUTHORIZED: invalid token");
                    }
                    if (redisService.isBlacklisted(token)) {
                        log.warn("[WS] CONNECT 거절: 블랙리스트 토큰");
                        throw new MessagingException("UNAUTHORIZED: blacklisted token");
                    }

                    String userId = String.valueOf(jwtProvider.getUserId(token));
                    accessor.setUser(new StompPrincipal(userId));

                    long[] hb = accessor.getHeartbeat();
                    log.info("[WS] CONNECT OK userId={} heartbeat={}",
                            userId, (hb == null ? "null" : Arrays.toString(hb)));
                }
            } else if (StompCommand.SUBSCRIBE.equals(cmd) || StompCommand.SEND.equals(cmd)) {
                String dest = accessor.getDestination();
                log.info("[WS] {} dest={}", cmd, dest);

                if (StompCommand.SEND.equals(cmd) && dest != null &&
                        (dest.startsWith("/topic") || dest.startsWith("/user"))) {
                    throw new MessagingException("FORBIDDEN: cannot SEND to broker destinations");
                }
            }

            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());

        } catch (MessagingException e) {
            throw e;
        } catch (Exception e) {
            log.error("[WS] preSend 예외", e);
            if (StompCommand.CONNECT.equals(cmd)) {
                throw new MessagingException("UNAUTHORIZED: interceptor failure");
            }
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }
    }

    private static String firstNonNull(String... values) {
        for (String v : values) if (v != null) return v;
        return null;
    }

    private static String safePrefix(String token) {
        if (token == null) return "null";
        int len = Math.min(12, token.length());
        return token.substring(0, len);
    }
}