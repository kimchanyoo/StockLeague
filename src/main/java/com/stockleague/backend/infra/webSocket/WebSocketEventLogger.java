package com.stockleague.backend.infra.webSocket;

import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class WebSocketEventLogger {

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event){
        Principal user = StompHeaderAccessor.wrap(event.getMessage()).getUser();
        log.info("[WebSocket 연결됨] user={}", user!=null?user.getName():"익명");
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        log.warn("[WebSocket 연결 종료됨] user={}", user != null ? user.getName() : "익명");
    }
}
