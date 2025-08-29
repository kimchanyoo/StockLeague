package com.stockleague.backend.user.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.TokenRedisService;
import com.stockleague.backend.infra.redis.UserRedisCleanupService;
import com.stockleague.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDeletionTxService {

    private final UserRepository userRepository;
    private final TokenRedisService tokenRedisService;
    private final UserRedisCleanupService userRedisCleanupService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void hardDeleteUser(Long userId) {
        int rows = userRepository.hardDeleteById(userId);
        if (rows == 0) throw new GlobalException(GlobalErrorCode.USER_NOT_FOUND);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void cleanupRedis(Long userId) {
        tokenRedisService.deleteRefreshToken(userId);
        userRedisCleanupService.purgeAllForUser(userId);
    }
}
