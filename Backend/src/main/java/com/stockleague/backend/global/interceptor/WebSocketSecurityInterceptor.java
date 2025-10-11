package com.stockleague.backend.global.interceptor;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.global.security.StompPrincipal;
import com.stockleague.backend.infra.redis.TokenRedisService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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
        StompCommand cmd = accessor.getCommand();
        if (cmd == null) return message;

        switch (cmd) {
            case CONNECT -> {
                String raw = firstNonNull(
                        accessor.getFirstNativeHeader("Authorization"),
                        accessor.getFirstNativeHeader("authorization")
                );

                String userId = null;
                if (raw != null) {
                    String token = normalizeBearer(raw);
                    if (token.isEmpty()) throw new MessagingException("UNAUTHORIZED: empty token");
                    if (!jwtProvider.validateToken(token)) throw new MessagingException("UNAUTHORIZED: invalid token");
                    if (redisService.isBlacklisted(token)) throw new MessagingException("UNAUTHORIZED: blacklisted token");
                    userId = String.valueOf(jwtProvider.getUserId(token));
                }
                if (userId == null && accessor.getSessionAttributes() != null) {
                    Object uidAttr = accessor.getSessionAttributes().get("ws.userId");
                    if (uidAttr != null) userId = uidAttr.toString();
                }

                accessor.setLeaveMutable(true);
                if (userId != null) {
                    Principal p = new StompPrincipal(userId);
                    accessor.setUser(p);
                    accessor.setHeader(SimpMessageHeaderAccessor.USER_HEADER, p);
                    if (accessor.getSessionAttributes() != null) {
                        accessor.getSessionAttributes().put("ws.userId", userId);
                    }
                    log.info("[WS] CONNECT authenticated: userId={}", userId);
                }
                return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
            }

            case SUBSCRIBE -> {
                boolean changed = restoreUserFromSessionIfPossible(accessor);

                String dest = accessor.getDestination();
                if (dest != null && dest.startsWith("/user/") && accessor.getUser() == null) {
                    log.warn("[WS] SUBSCRIBE 거절: /user/** 구독에 인증 필요 (dest={})", dest);
                    throw new MessagingException("FORBIDDEN: /user/** requires auth");
                }
                return changed
                        ? MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders())
                        : message;
            }

            case SEND -> {
                boolean changed = restoreUserFromSessionIfPossible(accessor);

                String dest = accessor.getDestination();
                if (dest != null && (dest.startsWith("/topic") || dest.startsWith("/user"))) {
                    log.warn("[WS] SEND 거절: 브로커 목적지로 직접 전송 시도 dest={}", dest);
                    throw new MessagingException("FORBIDDEN: cannot SEND to broker destinations");
                }
                if (dest != null && !dest.startsWith("/pub")) {
                    log.warn("[WS] SEND 거절: 허용되지 않은 목적지 dest={}", dest);
                    throw new MessagingException("FORBIDDEN: client must SEND to /pub/**");
                }
                return changed
                        ? MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders())
                        : message;
            }

            default -> {
                return message;
            }
        }
    }

    private boolean restoreUserFromSessionIfPossible(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null && accessor.getSessionAttributes() != null) {
            Object uid = accessor.getSessionAttributes().get("ws.userId");
            if (uid != null) {
                accessor.setLeaveMutable(true);
                Principal p = new StompPrincipal(uid.toString());
                accessor.setUser(p);
                accessor.setHeader(SimpMessageHeaderAccessor.USER_HEADER, p);
                return true;
            }
        }
        return false;
    }

    private static String normalizeBearer(String raw) {
        if (raw == null) return "";
        String v = raw.trim().replace("\"", "");
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) v = v.substring(7).trim();
        return v;
    }
}
