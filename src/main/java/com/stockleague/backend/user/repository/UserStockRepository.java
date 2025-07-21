package com.stockleague.backend.user.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserStock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStockRepository extends JpaRepository<UserStock, Long> {

    Optional<UserStock> findByUserAndStock(User user, Stock stock);

    /**
     * 특정 사용자가 보유한 모든 종목을 조회합니다.
     *
     * @param user 조회 대상 사용자
     * @return 보유 주식 리스트
     */
    List<UserStock> findByUser(User user);
}
