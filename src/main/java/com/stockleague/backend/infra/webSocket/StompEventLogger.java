package com.stockleague.backend.infra.webSocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
class StompEventLogger {

    private final SimpUserRegistry userRegistry;

    @EventListener
    public void onSubscribe(SessionSubscribeEvent e) {
        var sha = StompHeaderAccessor.wrap(e.getMessage());
        String sid = sha.getSessionId();
        String dest = sha.getDestination();
        String user = (sha.getUser() != null) ? sha.getUser().getName() : "null";
        log.info("[EVT] SUBSCRIBE sid={}, user={}, dest={}", sid, user, dest);
    }

    @EventListener
    public void onConnect(SessionConnectedEvent e) {
        var sha = StompHeaderAccessor.wrap(e.getMessage());
        log.info("[EVT] CONNECTED sid={}, user={}", sha.getSessionId(),
                sha.getUser() != null ? sha.getUser().getName() : "null");
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent e) {
        log.info("[EVT] DISCONNECT sid={}, closeStatus={}", e.getSessionId(), e.getCloseStatus());
    }
}
