package com.stockleague.backend.infra.webSocket;

import com.stockleague.backend.user.dto.response.UserAssetValuationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssetWebSocketPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(String userName, UserAssetValuationDto dto) {
        messagingTemplate.convertAndSendToUser(
                userName, "/queue/asset", dto
        );
    }
}
