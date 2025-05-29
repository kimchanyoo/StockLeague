package com.stockleague.backend.global.handler;

import com.stockleague.backend.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getHeader("Authorization");
            log.info("[HandshakeInterceptor] Authorization 헤더: {}", token);

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // "Bearer " 제거

                if (jwtProvider.validateToken(token)) {
                    Long userId = jwtProvider.getUserId(token);
                    attributes.put("userId", userId);
                    log.info("[HandshakeInterceptor] userId 설정 완료: {}", userId);
                } else {
                    log.warn("[HandshakeInterceptor] 유효하지 않은 토큰");
                }
            } else {
                log.warn("[HandshakeInterceptor] Authorization 헤더 누락 또는 형식 오류");
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {

    }
}
