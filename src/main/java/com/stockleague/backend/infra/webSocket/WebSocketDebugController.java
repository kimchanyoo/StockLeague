package com.stockleague.backend.infra.webSocket;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ws-debug")
public class WebSocketDebugController {

    private final SimpUserRegistry simpUserRegistry;

    @GetMapping("/active-users")
    public List<String> getActiveUsers() {
        List<String> users = simpUserRegistry.getUsers().stream()
                .map(SimpUser::getName)
                .toList();

        log.info("✅ 현재 SimpUserRegistry 등록된 유저 수: {}", users.size());
        users.forEach(u -> log.info(" - {}", u));

        return users;
    }
}
