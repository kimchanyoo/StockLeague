package com.stockleague.backend.global.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        Object userId = attributes.get("userId");
        if (userId != null) {
            log.info("[HandshakeHandler] Principal 연결됨: userId={}", userId);
            return () -> userId.toString(); // userId 기반 Principal
        }
        return null;
    }
}
