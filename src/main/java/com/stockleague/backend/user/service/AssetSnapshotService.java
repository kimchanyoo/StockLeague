package com.stockleague.backend.user.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.dto.response.UserAssetSnapshotDto;
import com.stockleague.backend.user.dto.response.UserAssetValuationDto;
import com.stockleague.backend.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetSnapshotService {

    private final UserRepository userRepository;
    private final UserAssetService userAssetService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String REDIS_KEY_PREFIX = "user:asset:closing:";
    private static final Duration SNAPSHOT_TTL = Duration.ofDays(30);

    /**
     * 전체 사용자에 대해 장 마감 기준 자산 스냅샷을 생성하고 Redis에 저장합니다.
     * <p>스냅샷 키 형식: {@code user:asset:closing:yyyy-MM-dd:{userId}}</p>
     */
    public void saveDailyAssetSnapshots() {
        LocalDate todayKst = LocalDate.now(ZoneId.of("Asia/Seoul"));
        String today = todayKst.toString();

        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            try {
                if(!user.isActive()) continue;

                UserAssetValuationDto live = userAssetService.getLiveAssetValuation(user.getId(), false);
                UserAssetSnapshotDto snapshot = UserAssetSnapshotDto.from(live);

                String redisKey = REDIS_KEY_PREFIX + today + ":" + user.getId();
                String json = objectMapper.writeValueAsString(snapshot);

                redisTemplate.opsForValue().set(redisKey, json);
                redisTemplate.expire(redisKey, SNAPSHOT_TTL);

                log.info("[Snapshot] 자산 스냅샷 저장 완료 - key={}, userId={}", redisKey, user.getId());

            } catch (Exception e) {
                log.error("[Snapshot] 자산 스냅샷 저장 실패 - userId={}, error={}", user.getId(), e.getMessage());
            }
        }
    }
}
