package com.stockleague.backend.global.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class AuthenticatedUserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            var httpRequest = servletRequest.getServletRequest();
            var session = httpRequest.getSession(false);
            if (session != null && httpRequest.getUserPrincipal() != null) {
                var principal = httpRequest.getUserPrincipal();
                attributes.put("userId", principal.getName()); // Principal 이름이 userId
                log.info("[HandshakeInterceptor] 세션 기반 userId 설정 완료: {}", principal.getName());
            } else {
                log.warn("[HandshakeInterceptor] 유저 세션이 존재하지 않음");
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {

    }
}
