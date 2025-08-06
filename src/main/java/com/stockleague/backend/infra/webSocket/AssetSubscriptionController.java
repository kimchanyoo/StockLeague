package com.stockleague.backend.infra.webSocket;

import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class AssetSubscriptionController {

    @SubscribeMapping("/user/queue/asset")
    public void onAssetSubscribe(Principal principal) {
        log.info("자산 구독 연결됨 - user: {}", principal.getName());
    }
}
