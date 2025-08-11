package com.stockleague.backend.global.interceptor;

import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.infra.redis.TokenRedisService;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        if (StompCommand.CONNECT.equals(command)) {
            log.debug("[WS] CONNECT 수신");

            String authHeader = firstNonNull(
                    accessor.getFirstNativeHeader("Authorization"),
                    accessor.getFirstNativeHeader("authorization"),
                    accessor.getFirstNativeHeader("accessToken")
            );

            if (authHeader == null || authHeader.isBlank()) {
                log.debug("[WS] CONNECT: Authorization 헤더 없음 → 익명으로 진행");
            } else {
                // 대소문자/공백 안전한 Bearer 파싱
                String raw = authHeader.trim();
                String token = raw.regionMatches(true, 0, "Bearer ", 0, 7)
                        ? raw.substring(7).trim()
                        : raw;

                try {
                    if (jwtProvider.validateToken(token) && !redisService.isBlacklisted(token)) {
                        Authentication auth = jwtProvider.getAuthentication(token);

                        // STOMP 세션의 사용자로 인증 객체 지정
                        accessor.setUser(auth);
                        // 세션 속성에 보관(프레임 복구용)
                        accessor.getSessionAttributes().put("user", auth);

                        // 스레드 로컬 SecurityContext에도 반영
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        log.info("[WS] CONNECT 인증 성공 - userId={}, principal={}", auth.getName(), auth.getName());
                    } else {
                        log.warn("[WS] CONNECT: 토큰 검증 실패 또는 블랙리스트 → 익명으로 진행");
                    }
                } catch (Exception e) {
                    log.warn("[WS] CONNECT: 토큰 처리 중 예외 → 익명으로 진행. msg={}", e.getMessage());
                }
            }
        } else if (StompCommand.DISCONNECT.equals(command)) {
            // 세션 종료 시 보안 컨텍스트 정리
            SecurityContextHolder.clearContext();
            log.debug("[WS] DISCONNECT 처리 - SecurityContext cleared");
        } else {
            // 다른 프레임에서 user가 비어있으면 세션에 저장된 사용자 복구
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (accessor.getUser() == null && sessionAttributes != null) {
                Object user = sessionAttributes.get("user");
                if (user instanceof Principal principal) {
                    accessor.setUser(principal);
                }
            }
        }

        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }


    /**
     * 전달된 값들 중 가장 먼저 null이 아닌 값을 반환합니다.
     * 모든 값이 null이면 null을 반환합니다.
     *
     * @param values 확인할 문자열 목록
     * @return 첫 번째 null이 아닌 문자열, 없으면 null
     */
    private static String firstNonNull(String... values) {
        return Stream.of(values)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
