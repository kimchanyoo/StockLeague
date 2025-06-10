package com.stockleague.backend.infra.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class WebSocketTestController {

    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketTestController(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/test")
    public void testEcho(String message, Principal principal) {
        log.info(">>> 받은 메시지: {}", message);
        log.info(">>> Principal: {}", principal);

        if (principal != null) {
            String username = principal.getName();
            String destination = "/user/" + username + "/queue/notifications";
            String payload = "서버에서 보낸 메시지: \"" + message + "\"";

            log.info(">>> [{}] 에게 메시지 전송: {}", destination, payload);
            messagingTemplate.convertAndSendToUser(username, "/queue/notifications", payload);
        } else {
            log.warn(">>> Principal이 null입니다. 메시지를 보낼 수 없습니다.");
        }
    }
}
