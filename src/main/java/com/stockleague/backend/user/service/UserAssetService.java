package com.stockleague.backend.user.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.StockPriceRedisService;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import com.stockleague.backend.stock.repository.ReservedCashRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserAsset;
import com.stockleague.backend.user.domain.UserStock;
import com.stockleague.backend.user.dto.response.StockValuationDto;
import com.stockleague.backend.user.dto.response.UserAssetValuationDto;
import com.stockleague.backend.user.repository.UserRepository;
import com.stockleague.backend.user.repository.UserStockRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAssetService {

    private final UserRepository userRepository;
    private final UserStockRepository userStockRepository;
    private final StockPriceRedisService stockPriceRedisService;
    private final ReservedCashRepository reservedCashRepository;

    /**
     * Redis의 현재가를 기반으로 사용자 보유 자산을 실시간 계산하여 반환합니다.
     *
     * @param userId 사용자 ID
     * @return 전체 자산 평가 정보 DTO
     * @throws GlobalException 다음과 같은 예외가 발생할 수 있습니다:
     *                         <ul>
     *                             <li>{@code USER_NOT_FOUND} - 사용자가 존재하지 않는 경우</li>
     *                             <li>{@code USER_ASSET_NOT_FOUND} - 사용자의 자산 정보가 존재하지 않은 경우</li>
     *                         </ul>
     */
    @Transactional(readOnly = true)
    public UserAssetValuationDto getLiveAssetValuation(Long userId, boolean isMarketOpen) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        UserAsset asset = user.getUserAsset();
        if (asset == null) {
            throw new GlobalException(GlobalErrorCode.USER_ASSET_NOT_FOUND);
        }

        BigDecimal availableCash = asset.getCashBalance();
        BigDecimal reservedCash = reservedCashRepository.sumUnrefundedByUser(user);

        List<UserStock> userStocks = userStockRepository.findByUser(user);
        List<StockValuationDto> stockDtos = new ArrayList<>();

        for (UserStock us : userStocks) {
            Stock stock = us.getStock();
            String ticker = stock.getStockTicker();
            String stockName = stock.getStockName();
            StockPriceDto latestPrice = stockPriceRedisService.getLatest(ticker);
            BigDecimal totalQuantity = us.getQuantity().add(us.getLockedQuantity());

            if (latestPrice == null || totalQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            StockValuationDto stockValuation = StockValuationDto.of(
                    ticker,
                    stockName,
                    totalQuantity,
                    us.getAvgBuyPrice(),
                    BigDecimal.valueOf(latestPrice.currentPrice())
            );
            stockDtos.add(stockValuation);
        }

        return UserAssetValuationDto.of(availableCash, stockDtos, isMarketOpen, reservedCash);
    }
}
