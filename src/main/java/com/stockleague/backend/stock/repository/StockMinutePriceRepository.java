package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockMinutePrice;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockMinutePriceRepository extends JpaRepository<StockMinutePrice, Long> {

    /**
     * 주어진 종목, 분봉 간격, 기준 시간에 해당하는 데이터가 이미 존재하는지 여부를 반환
     *
     * @param stock      종목 엔티티
     * @param interval   분봉 간격
     * @param candleTime 기준 시간
     * @return 존재 여부 (true: 존재함)
     */
    boolean existsByStockAndIntervalAndCandleTime(Stock stock, int interval, LocalDateTime candleTime);


    /**
     * 주어진 종목 ID, 분봉 간격(interval)에 해당하는 분봉 데이터를
     * 최신 시간(candleTime) 순으로 페이징 조회한다.
     *
     * @param stockId    종목의 ID
     * @param interval   분봉 간격 (예: 1, 3, 5, 10, 15, 30, 60)
     * @param pageable   페이징 정보 (offset, limit 포함)
     * @return 분봉 데이터 페이지 (최신 순 정렬)
     */
    @Query("""
                SELECT m
                FROM StockMinutePrice m
                WHERE m.stock.id = :stockId
                  AND m.interval = :interval
                ORDER BY m.candleTime DESC
            """)
    Page<StockMinutePrice> findAllByStockIdAndIntervalOrderByCandleTimeDesc(
            Long stockId,
            int interval,
            Pageable pageable
    );
}
