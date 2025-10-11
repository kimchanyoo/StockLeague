package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockDailyPrice;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDailyPriceRepository extends JpaRepository<StockDailyPrice, Long> {

    /**
     * 특정 종목에 대해 지정된 날짜의 일봉 데이터가 존재하는지 확인
     *
     * @param stock 종목 엔티티
     * @param date  확인할 날짜
     * @return 해당 종목의 해당 날짜 일봉 존재 여부
     */
    boolean existsByStockAndDate(Stock stock, LocalDate date);

    /**
     * 특정 종목의 일봉 데이터를 날짜 기준 최신순으로 페이징 조회
     *
     * @param stockId 종목 ID
     * @param pageable 페이징 정보
     * @return 최신순 일봉 데이터 페이지
     */
    @Query("""
                SELECT d
                FROM StockDailyPrice d
                WHERE d.stock.id = :stockId
                ORDER BY d.date DESC
            """)
    Page<StockDailyPrice> findAllByStockIdOrderByDateDesc(Long stockId, Pageable pageable);

    /**
     * <p>특정 종목의 주어진 날짜 구간(start ~ end)의 일봉 데이터를 날짜 오름차순으로 조회</p>
     * 주봉/월봉/연봉 생성 시 open, close, high, low, volume 계산에 사용
     *
     * @param stock 종목 엔티티
     * @param start 시작 날짜 (포함)
     * @param end   종료 날짜 (포함)
     * @return 날짜 오름차순의 일봉 데이터 리스트
     */
    List<StockDailyPrice> findAllByStockAndDateBetweenOrderByDateAsc(
            Stock stock, LocalDate start, LocalDate end);
}
