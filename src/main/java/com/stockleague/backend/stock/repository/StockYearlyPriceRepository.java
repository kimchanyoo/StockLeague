package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockYearlyPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockYearlyPriceRepository extends JpaRepository<StockYearlyPrice, Long> {

    /**
     * 특정 종목의 연봉 데이터를 연도 기준으로 내림차순 정렬하여 페이징 조회
     *
     * @param stockId  종목 ID
     * @param pageable 페이징 정보 (PageRequest.of(page, size))
     * @return 연도 내림차순 정렬된 연봉 Page 결과
     */
    @Query("""
                SELECT y
                FROM StockYearlyPrice y
                WHERE y.stock.id = :stockId
                ORDER BY y.year DESC
            """)
    Page<StockYearlyPrice> findAllByStockIdOrderByYearDesc(Long stockId, Pageable pageable);

    /**
     * 특정 종목에 대해 해당 연도의 연봉 데이터가 이미 존재하는지 여부를 확인
     *
     * @param stock 종목 엔티티
     * @param year  연도 (예: 2024)
     * @return 존재 여부 (true: 존재함, false: 없음)
     */
    boolean existsByStockAndYear(Stock stock, int year);
}
