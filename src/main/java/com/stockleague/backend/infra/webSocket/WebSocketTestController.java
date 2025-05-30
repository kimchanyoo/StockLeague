package com.stockleague.backend.infra.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class WebSocketTestController {

    @MessageMapping("/test")
    @SendToUser("/queue/notifications")
    public String testEcho(String message, Principal principal) {
        log.info("[Echo] from userId={}, message={}", principal.getName(), message);
        return "echo: " + message;
    }
}
