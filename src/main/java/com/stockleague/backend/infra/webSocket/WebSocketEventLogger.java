package com.stockleague.backend.infra.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.*;

@Slf4j
@Component
public class WebSocketEventLogger {

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String auth = sha.getFirstNativeHeader("Authorization");
        log.info("[WS] CONNECT try: sessionId={}, authHeaderPresent={}", sessionId, auth != null);
    }

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String user = (sha.getUser() != null ? sha.getUser().getName() : "익명");
        log.info("[WS] CONNECTED: sessionId={}, user={}", sessionId, user);
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String user = (sha.getUser() != null ? sha.getUser().getName() : "익명");
        String dest = sha.getDestination();
        log.info("[WS] SUBSCRIBE: sessionId={}, user={}, dest={}", sessionId, user, dest);
    }

    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        String user = (sha.getUser() != null ? sha.getUser().getName() : "익명");
        String id = sha.getSubscriptionId();
        log.info("[WS] UNSUBSCRIBE: sessionId={}, user={}, subscriptionId={}", sessionId, user, id);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String user = (event.getUser() != null ? event.getUser().getName() : "익명");
        CloseStatus status = event.getCloseStatus();
        log.info("[WS] DISCONNECT: sessionId={}, user={}, code={}, reason={}",
                sessionId, user, status.getCode(), status.getReason());
    }
}
