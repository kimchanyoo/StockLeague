package com.stockleague.backend.user.controller;

import com.stockleague.backend.user.dto.response.UserProfitRateRankingListDto;
import com.stockleague.backend.user.service.UserRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ranking")
@Tag(name = "Ranking", description = "유저 수익률 랭킹 관련 API")
public class UserRankingController {

    private final UserRankingService userRankingService;

    @GetMapping("/profit-rate")
    @Operation(
            summary = "수익률 랭킹 조회",
            description = """
                    유저들의 수익률 기준 랭킹을 전체 반환합니다.
                    - 장중에는 Redis에 저장된 현재가 기준 실시간 자산 정보를 이용해 수익률을 계산합니다.
                    - 장 마감 이후에는 Redis에 저장된 자산 스냅샷 정보를 기반으로 수익률을 계산합니다.
                    - 응답에는 전체 랭킹 리스트와 로그인 사용자의 순위 정보가 포함됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "수익률 랭킹 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = UserProfitRateRankingListDto.class),
                            examples = @ExampleObject(
                                    name = "UserProfitRateRankingList",
                                    summary = "수익률 랭킹 응답 예시",
                                    value = """
                                            {
                                              "rankingList": [
                                                {
                                                  "userId": 1,
                                                  "nickname": "stockMaster",
                                                  "profitRate": "12.45",
                                                  "totalAsset": "184000000",
                                                  "ranking": 1
                                                },
                                                {
                                                  "userId": 2,
                                                  "nickname": "investKing",
                                                  "profitRate": "7.80",
                                                  "totalAsset": "165000000",
                                                  "ranking": 2
                                                }
                                              ],
                                              "myRanking": {
                                                "userId": 2,
                                                "nickname": "investKing",
                                                "profitRate": "7.80",
                                                "totalAsset": "165000000",
                                                "ranking": 2
                                              },
                                              "totalCount": 2,
                                              "isMarketOpen": true
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<UserProfitRateRankingListDto> getProfitRateRanking(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userRankingService.getProfitRateRanking(userId));
    }
}
