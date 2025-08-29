//package com.stockleague.backend.infra.webSocket;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.event.EventListener;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.messaging.*;
//
//import java.security.Principal;
//import java.util.Map;
//
//@Slf4j
//@Component
//public class WebSocketEventLogger implements ChannelInterceptor {
//
//    private static String pname(Principal p) { return (p == null ? "null" : p.getName()); }
//    private static Object attr(Map<String, Object> m, String k) { return (m == null ? null : m.get(k)); }
//
//    private static void logEnvelope(String prefix, Message<?> msg) {
//        StompHeaderAccessor sha = StompHeaderAccessor.wrap(msg);
//        Map<String, Object> attrs = sha.getSessionAttributes();
//        Object simpUserHeader = sha.getHeader(SimpMessageHeaderAccessor.USER_HEADER);
//        log.info("{}: sessionId={}, command={}, accessorUser={}, eventUser=N/A, simpUserHeader={}, attr.userId={}, attr.principal={}",
//                prefix,
//                sha.getSessionId(),
//                sha.getCommand(),
//                pname(sha.getUser()),
//                (simpUserHeader instanceof Principal sp ? sp.getName() : String.valueOf(simpUserHeader)),
//                attr(attrs, "ws.userId"),
//                attr(attrs, "ws.principal"));
//    }
//
//    @EventListener
//    public void onConnect(SessionConnectEvent event) {
//        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
//        String sessionId = sha.getSessionId();
//        String auth = sha.getFirstNativeHeader("Authorization");
//        log.info("[WS] CONNECT try: sessionId={}, authHeaderPresent={}", sessionId, auth != null);
//        logEnvelope("[WS] CONNECT env", event.getMessage());
//    }
//
//    @EventListener
//    public void onConnected(SessionConnectedEvent event) {
//        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
//        String sessionId = sha.getSessionId();
//        Principal accessorUser = sha.getUser();
//        Principal eventUser = event.getUser(); // 중요: 이벤트가 가진 user
//        Object simpUserHeader = sha.getHeader(SimpMessageHeaderAccessor.USER_HEADER);
//
//        log.info("[WS] CONNECTED: sessionId={}, accessorUser={}, eventUser={}, simpUserHeader={}",
//                sessionId, pname(accessorUser), pname(eventUser),
//                (simpUserHeader instanceof Principal sp ? sp.getName() : String.valueOf(simpUserHeader)));
//        logEnvelope("[WS] CONNECTED env", event.getMessage());
//    }
//
//    @EventListener
//    public void onSubscribe(SessionSubscribeEvent event) {
//        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
//        String sessionId = sha.getSessionId();
//        String dest = sha.getDestination();
//        String subId = sha.getSubscriptionId();
//
//        Principal accessorUser = sha.getUser();
//        Principal eventUser = event.getUser(); // 여기가 null이면 simpUser 전파 문제 의심
//        Object simpUserHeader = sha.getHeader(SimpMessageHeaderAccessor.USER_HEADER);
//
//        Map<String, Object> attrs = sha.getSessionAttributes();
//
//        log.info("[WS] SUBSCRIBE: sessionId={}, dest={}, subId={}, accessorUser={}, eventUser={}, simpUserHeader={}, attr.userId={}, attr.principal={}",
//                sessionId, dest, subId,
//                pname(accessorUser), pname(eventUser),
//                (simpUserHeader instanceof Principal sp ? sp.getName() : String.valueOf(simpUserHeader)),
//                attr(attrs, "ws.userId"), attr(attrs, "ws.principal"));
//    }
//
//    @EventListener
//    public void onUnsubscribe(SessionUnsubscribeEvent event) {
//        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
//        String sessionId = sha.getSessionId();
//        String id = sha.getSubscriptionId();
//        Principal accessorUser = sha.getUser();
//        Principal eventUser = event.getUser();
//        log.info("[WS] UNSUBSCRIBE: sessionId={}, subscriptionId={}, accessorUser={}, eventUser={}",
//                sessionId, id, pname(accessorUser), pname(eventUser));
//    }
//
//    @EventListener
//    public void onDisconnect(SessionDisconnectEvent event) {
//        String sessionId = event.getSessionId();
//        Principal eventUser = event.getUser();
//        CloseStatus status = event.getCloseStatus();
//        log.info("[WS] DISCONNECT: sessionId={}, eventUser={}, code={}, reason={}",
//                sessionId, pname(eventUser), status.getCode(), status.getReason());
//    }
//}
