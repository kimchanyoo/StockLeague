package com.stockleague.backend.user.scheduler;

import com.stockleague.backend.user.service.AssetSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetSnapshotScheduler {

    private final AssetSnapshotService assetSnapshotService;

    /**
     * 매일 평일 오후 15시 30분에 실행됩니다.
     * <p>한국 주식 시장 마감 시간에 맞춰 Redis에 자산 스냅샷을 저장합니다.</p>
     */
    @Scheduled(cron = "0 30 15 * * MON-FRI", zone = "Asia/Seoul")
    public void saveClosingSnapshots() {
        log.info("[스케줄러] 장 마감 자산 스냅샷 저장 시작");
        assetSnapshotService.saveDailyAssetSnapshots();
    }
}
