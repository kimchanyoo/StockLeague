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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetLivePublisherScheduler {

    private final UserRepository userRepository;
    private final UserAssetService userAssetService;
    private final AssetWebSocketPublisher publisher;

    @Scheduled(fixedRate = 1000)
    public void pushLiveAssetForAllUsers() {
        if (isMarketClosed()) return;

        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                UserAssetValuationDto dto = userAssetService.getLiveAssetValuation(user.getId(), true);
                String userName = "user" + user.getId();
                publisher.sendToUser(userName, dto);
            } catch (Exception e) {
                log.warn("[실시간 자산] 푸시 실패 - userId={}, err={}", user.getId(), e.getMessage());
            }
        }
    }
}
