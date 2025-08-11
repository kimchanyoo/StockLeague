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

    private final UserRepository userRepository;
    private final UserAssetService userAssetService;
    private final AssetWebSocketPublisher publisher;

    private final SimpUserRegistry simpUserRegistry;

    @Scheduled(fixedRate = 1000)
    public void pushLiveAssetForConnectedUsers() {
        if (isMarketClosed()) return;

        for (SimpUser simpUser : simpUserRegistry.getUsers()) {
            String userName = simpUser.getName();

            log.info("[대상 유저] userName: {}, 세션 수: {}", userName, simpUser.getSessions().size());

            try {
                Long userId = extractUserId(userName);
                UserAssetValuationDto dto = userAssetService.getLiveAssetValuation(userId, true);
                publisher.sendToUser(userName, dto);

                log.info("[전송 성공] userName={}, userId={}, 평가금액={}, 수익률={}",
                        userName, userId, dto.getTotalAsset(), dto.getTotalProfitRate());
            } catch (Exception e) {
                log.warn("[실시간 자산] 푸시 실패 - userId={}, err={}", userName, e.getMessage());
            }
        }
    }

    private Long extractUserId(String userName) {
        return Long.parseLong(userName.replace("user", ""));
    }
}
