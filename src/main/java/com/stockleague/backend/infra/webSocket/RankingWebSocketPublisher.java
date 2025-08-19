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

    private static final String DEST_TOPIC_ALL = "/topic/ranking";
    private static final String DEST_QUEUE_ME  = "/queue/ranking/me";

    /** 전체 랭킹 브로드캐스트 (추천 사용) */
    public void publishAll(List<UserProfitRateRankingDto> rankingList, boolean marketOpen) {
        UserProfitRateRankingMessage payload = UserProfitRateRankingMessage.broadcast(rankingList, marketOpen);
        messagingTemplate.convertAndSend(DEST_TOPIC_ALL, payload);
    }

    /** 개인 랭킹 전송: /user/{userName}/queue/ranking/me */
    public void publishMyRanking(String userName, UserProfitRateRankingDto myRanking, boolean marketOpen) {
        UserProfitRateRankingMessage payload = UserProfitRateRankingMessage.personal(myRanking, marketOpen);
        messagingTemplate.convertAndSendToUser(userName, DEST_QUEUE_ME, payload);
    }
}
