package com.stockleague.backend.infra.webSocket;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ws-debug")
public class WebSocketDebugController {

    private final SimpUserRegistry simpUserRegistry;

    @GetMapping("/active-users")
    public List<String> getActiveUsers() {
        return simpUserRegistry.getUsers().stream()
                .map(SimpUser::getName)
                .toList();
    }
}
