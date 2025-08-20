package com.stockleague.backend.infra.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@Controller
public class WebSocketTestController {

    public record PongDto(String type, String echo, long ts) {}

    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketTestController(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/test")
    public void testEcho(String message, Principal principal) {
        log.info(">>> 받은 메시지: {}", message);
        if (principal == null) {
            log.warn("Principal is null! (미인증 상태의 STOMP 메시지)");
            return;
        }
        final String username = principal.getName();
        log.info(">>> Principal.getName() = {}", username);

        PongDto dto = new PongDto("PONG", message, System.currentTimeMillis());

        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();
        headers.setContentType(MimeTypeUtils.APPLICATION_JSON);
        headers.setLeaveMutable(true);

        try {
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    dto,
                    headers.getMessageHeaders()
            );
            log.info("[/user/{}/queue/notifications] JSON 전송 성공: type={}, echoLen={}, ts={}",
                    username, dto.type(), dto.echo().length(), dto.ts());
        } catch (Exception e) {
            log.error("[/user/{}/queue/notifications] 전송 실패", username, e);
        }
    }

    @MessageMapping("/broadcast")
    public void broadcastTest(String message) {
        log.info(">>> 브로드캐스트 메시지 수신: {}", message);

        String payload = "전체 사용자에게 보낸 메시지: \"" + message + "\"";
        messagingTemplate.convertAndSend("/topic/broadcast", payload);
        log.info("[/topic/broadcast] 에게 브로드캐스트 전송 성공: {}", payload);
    }
}
