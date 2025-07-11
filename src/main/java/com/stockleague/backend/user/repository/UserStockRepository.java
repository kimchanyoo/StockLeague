package com.stockleague.backend.user.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserStock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStockRepository extends JpaRepository<UserStock, Long> {

    Optional<UserStock> findByUserAndStock(User user, Stock stock);
}
