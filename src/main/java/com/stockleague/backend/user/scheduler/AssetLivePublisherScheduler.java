package com.stockleague.backend.user.scheduler;

import static com.stockleague.backend.global.util.MarketTimeUtil.isMarketClosed;

import com.stockleague.backend.infra.webSocket.AssetWebSocketPublisher;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.dto.response.UserAssetValuationDto;
import com.stockleague.backend.user.repository.UserRepository;
import com.stockleague.backend.user.service.UserAssetService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetLivePublisherScheduler {

    private final UserAssetService userAssetService;
    private final AssetWebSocketPublisher publisher;

    private final SimpUserRegistry simpUserRegistry;

    @Scheduled(fixedRate = 1000)
    public void pushLiveAssetForConnectedUsers() {
        if (isMarketClosed()) return;

        for (SimpUser simpUser : simpUserRegistry.getUsers()) {
            String principalName = simpUser.getName();

            try {
                Long userId = parseUserIdStrict(principalName);
                UserAssetValuationDto dto = userAssetService.getLiveAssetValuation(userId, true);
                publisher.sendToUser(principalName, dto);
            } catch (Exception e) {
                log.warn("[실시간 자산] 푸시 실패 - principalName={}, err={}", principalName, e.getMessage());
            }
        }
    }

    private Long parseUserIdStrict(String principalName) {
        return Long.parseLong(principalName);
    }
}
