package com.stockleague.backend.apiTest;

import static com.stockleague.backend.user.domain.OauthServerType.KAKAO;

import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserAsset;
import com.stockleague.backend.user.domain.UserRole;
import com.stockleague.backend.user.repository.UserAssetRepository;
import com.stockleague.backend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestUserService {

    private final UserRepository userRepository;
    private final UserAssetRepository userAssetRepository;
    private final JwtProvider jwtProvider;

    @Data
    @Builder
    public static class TokenItem{
        private Long userId;
        private String nickname;
        private String accessToken;
    }

    @Transactional
    public List<TokenItem> testUserSignUp() {

        List<TokenItem> result = new ArrayList<>();

        for(int i = 0 ; i < 100; i++){
            String nickname = "임시유저" + i;

            User user = userRepository.save(User.builder()
                    .oauthId(randomNumber())
                    .provider(KAKAO)
                    .nickname(nickname)
                    .role(UserRole.USER)
                    .agreedToTerms(true)
                    .isOverFifteen(true)
                    .build()
            );

            UserAsset userAsset = userAssetRepository.save(UserAsset.builder()
                    .user(user)
                    .userId(user.getId())
                    .cashBalance(BigDecimal.valueOf(10_000_000L))
                    .updatedAt(LocalDateTime.now())
                    .build()
            );

            user.setUserAsset(userAsset);

            String accessToken = jwtProvider.createTestAccessToken(user.getId(), 10);

            result.add(TokenItem.builder()
                    .userId(user.getId())
                    .nickname(nickname)
                    .accessToken(accessToken)
                    .build()
            );
        }

        return result;
    }

    private String randomNumber() {
        Random random = new Random();
        return String.valueOf(1000000000 + random.nextLong(9000000000L));
    }
}
