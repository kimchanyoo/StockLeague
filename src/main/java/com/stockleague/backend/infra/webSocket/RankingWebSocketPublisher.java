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

    private static final String DEST_TOPIC_ALL = "/topic/ranking/profit";
    private static final String DEST_QUEUE_ME  = "/queue/ranking/me/profit";

    private static final String DEST_TOPIC_ALL_ASSET = "/topic/ranking/asset";
    private static final String DEST_QUEUE_ME_ASSET  = "/queue/ranking/me/asset";

    /** 전체 랭킹 브로드캐스트 (수익률) */
    public void publishAllByProfit(List<UserProfitRateRankingDto> rankingList, boolean marketOpen) {
        UserProfitRateRankingMessage payload = UserProfitRateRankingMessage.broadcast(rankingList, marketOpen);
        messagingTemplate.convertAndSend(DEST_TOPIC_ALL, payload);
    }

    /** 개인 랭킹 전송 (수익률): /user/{userName}/queue/ranking/me/profit */
    public void publishMyRankingByProfit(String userName, UserProfitRateRankingDto myRanking, boolean marketOpen) {
        UserProfitRateRankingMessage payload = UserProfitRateRankingMessage.personal(myRanking, marketOpen);
        messagingTemplate.convertAndSendToUser(userName, DEST_QUEUE_ME, payload);
    }

    /** 전체 랭킹 브로드캐스트 (총자산) */
    public void publishAllByAsset(List<UserProfitRateRankingDto> rankingList, boolean marketOpen) {
        UserProfitRateRankingMessage payload = UserProfitRateRankingMessage.broadcast(rankingList, marketOpen);
        messagingTemplate.convertAndSend(DEST_TOPIC_ALL_ASSET, payload);
    }

    /** 개인 랭킹 전송 (수익률): /user/{userName}/queue/ranking/me/asset */
    public void publishMyAssetRanking(String userName, UserProfitRateRankingDto myRanking, boolean marketOpen) {
        UserProfitRateRankingMessage payload = UserProfitRateRankingMessage.personal(myRanking, marketOpen);
        messagingTemplate.convertAndSendToUser(userName, DEST_QUEUE_ME_ASSET, payload);
    }
}
