package com.stockleague.backend.infra.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class WebSocketTestController {

    public record PongDto(String type, String echo, long ts) {}

    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketTestController(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 클라이언트가 /pub/test 로 SEND 하면,
     * 같은 username의 모든 STOMP 세션(/user/queue/notifications 구독)으로 PONG 전송.
     */
    @MessageMapping("/test")
    public void testEcho(String message,
                         Principal principal,
                         @Header(name = "simpSessionId", required = false) String sessionId) {

        if (principal == null) {
            log.warn("Principal is null! (미인증 STOMP 메시지)");
            return;
        }

        final String username = principal.getName();
        final String echo = (message == null) ? "" : message;
        final PongDto dto = new PongDto("PONG", echo, System.currentTimeMillis());

        log.info(">>> 받은 메시지: '{}', principal={}, simpSessionId={}", echo, username, sessionId);

        try {
            // 세션 타깃팅 헤더 제거 → 동일 username의 모든 세션으로 전달
            messagingTemplate.convertAndSendToUser(username, "/queue/notifications", dto);
            log.info("[/user/{}/queue/notifications] 전송 성공 | broadcast-to-all-sessions | type={}, echoLen={}, ts={}",
                    username, dto.type(), dto.echo().length(), dto.ts());
        } catch (Exception e) {
            log.error("[/user/{}/queue/notifications] 전송 실패", username, e);
        }
    }

    /**
     * 브로드캐스트 테스트 (/topic/broadcast 구독 탭 모두 수신)
     */
    @MessageMapping("/broadcast")
    public void broadcastTest(String message,
                              @Header(name = "simpSessionId", required = false) String sessionId) {

        final String echo = (message == null) ? "" : message;
        log.info(">>> 브로드캐스트 메시지 수신: '{}', simpSessionId={}", echo, sessionId);

        messagingTemplate.convertAndSend("/topic/broadcast",
                "전체 사용자에게 보낸 메시지: \"" + echo + "\"");

        log.info("[/topic/broadcast] 브로드캐스트 전송 성공");
    }
}
