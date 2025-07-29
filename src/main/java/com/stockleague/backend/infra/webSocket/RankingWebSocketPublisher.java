package com.stockleague.backend.infra.webSocket;

import com.stockleague.backend.user.dto.response.UserProfitRateRankingDto;
import com.stockleague.backend.user.dto.response.UserProfitRateRankingMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String DESTINATION = "/topic/ranking";

    public void publish(List<UserProfitRateRankingDto> rankingList, UserProfitRateRankingDto myRanking) {
        UserProfitRateRankingMessage message = UserProfitRateRankingMessage.from(rankingList, myRanking);
        messagingTemplate.convertAndSend(DESTINATION, message);
    }
}
