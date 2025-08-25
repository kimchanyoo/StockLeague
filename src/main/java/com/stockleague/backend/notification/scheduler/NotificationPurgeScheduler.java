package com.stockleague.backend.notification.scheduler;

import com.stockleague.backend.notification.service.NotificationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPurgeScheduler {

    private final NotificationService service;
    private static final int RETENTION_DAYS = 7;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void purge() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deleted = service.purgeAll(threshold);

        if (deleted > 0) {
            log.info("[NotificationPurge] {}개 알림 삭제 완료 (threshold: {})", deleted, threshold);
        } else {
            log.debug("[NotificationPurge] 삭제 대상 없음 (threshold: {})", threshold);
        }
    }
}
