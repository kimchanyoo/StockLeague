package com.stockleague.backend.global.interceptor;

import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.global.security.StompPrincipal;
import com.stockleague.backend.infra.redis.TokenRedisService;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final String ATTR_USER_ID   = "ws.userId";
    private static final String ATTR_PRINCIPAL = "ws.principal";

    private final JwtProvider jwtProvider;
    private final TokenRedisService redisService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        accessor.setLeaveMutable(true);

        StompCommand cmd = accessor.getCommand();
        if (cmd == null) return message;

        try {
            switch (cmd) {
                case CONNECT    -> handleConnect(accessor);
                case SUBSCRIBE  -> handleSubscribe(accessor);
                case SEND       -> handleSend(accessor);
                case DISCONNECT -> handleDisconnect(accessor);
                default -> { /* NOP */ }
            }
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());

        } catch (MessagingException e) {
            throw e;
        } catch (Exception e) {
            log.error("[WS] preSend 예외", e);
            throw new MessagingException("WS interceptor failure");
        }
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        Map<String, ?> headers = accessor.toNativeHeaderMap();
        log.info("[WS] CONNECT sessionId={} headers={}", accessor.getSessionId(), headers);

        String authHeader = firstNonNull(
                accessor.getFirstNativeHeader("Authorization"),
                accessor.getFirstNativeHeader("authorization"),
                accessor.getFirstNativeHeader("accessToken")
        );

        ensureSessionAttributes(accessor);

        if (authHeader == null || authHeader.isBlank()) {
            log.warn("[WS] CONNECT: Authorization 없음 → 익명 허용(임시)");
            accessor.getSessionAttributes().put(ATTR_USER_ID, null);
            accessor.getSessionAttributes().put(ATTR_PRINCIPAL, null);
            return;
        }

        String raw = authHeader.trim().replace("\"", "");
        String token = raw.regionMatches(true, 0, "Bearer ", 0, 7)
                ? raw.substring(7).trim()
                : raw;

        if (!jwtProvider.validateToken(token)) {
            log.warn("[WS] CONNECT 거절: JWT 검증 실패 tokenPrefix={}", safePrefix(token));
            throw new MessagingException("UNAUTHORIZED: invalid token");
        }
        if (redisService.isBlacklisted(token)) {
            log.warn("[WS] CONNECT 거절: 블랙리스트 토큰");
            throw new MessagingException("UNAUTHORIZED: blacklisted token");
        }

        String userId = String.valueOf(jwtProvider.getUserId(token));
        StompPrincipal principal = new StompPrincipal(userId);

        accessor.setUser(principal);
        accessor.getSessionAttributes().put(ATTR_USER_ID, userId);
        accessor.getSessionAttributes().put(ATTR_PRINCIPAL, principal);

        long[] hb = accessor.getHeartbeat();
        log.info("[WS] CONNECT OK sessionId={} userId={} heartbeat={}",
                accessor.getSessionId(), userId, (hb == null ? "null" : Arrays.toString(hb)));
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        ensureSessionAttributes(accessor);
        restorePrincipalIfMissing(accessor);

        String dest = accessor.getDestination();
        String user = accessor.getUser() == null ? "익명" : accessor.getUser().getName();
        log.info("[WS] SUBSCRIBE sessionId={} dest={} user={}",
                accessor.getSessionId(), dest, user);

        if (dest != null && dest.startsWith("/user/") && accessor.getUser() == null) {
            throw new MessagingException("FORBIDDEN: subscription to /user/** requires authentication");
        }
    }

    private void handleSend(StompHeaderAccessor accessor) {
        ensureSessionAttributes(accessor);
        restorePrincipalIfMissing(accessor);

        String dest = accessor.getDestination();
        String user = accessor.getUser() == null ? "익명" : accessor.getUser().getName();
        log.info("[WS] SEND sessionId={} dest={} user={}",
                accessor.getSessionId(), dest, user);

        if (dest != null && (dest.startsWith("/topic") || dest.startsWith("/user"))) {
            throw new MessagingException("FORBIDDEN: cannot SEND to broker destinations");
        }
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        String user = accessor.getUser() == null ? "익명" : accessor.getUser().getName();
        log.info("[WS] DISCONNECT sessionId={} user={}", accessor.getSessionId(), user);
    }

    /** CONNECT 때 저장한 Principal을 SUBSCRIBE/SEND에서 복원 */
    private void restorePrincipalIfMissing(StompHeaderAccessor accessor) {
        if (accessor.getUser() != null) return;
        var attrs = accessor.getSessionAttributes();
        if (attrs == null) return;

        Object p = attrs.get(ATTR_PRINCIPAL);
        if (p instanceof StompPrincipal sp) {
            accessor.setUser(sp);
            return;
        }
        Object uid = attrs.get(ATTR_USER_ID);
        if (uid instanceof String s && !s.isBlank()) {
            accessor.setUser(new StompPrincipal(s));
        }
    }

    /** 세션 속성 맵이 null인 경우를 방지 */
    @SuppressWarnings("unchecked")
    private void ensureSessionAttributes(StompHeaderAccessor accessor) {
        if (accessor.getSessionAttributes() == null) {
            accessor.setSessionAttributes(new ConcurrentHashMap<>());
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
