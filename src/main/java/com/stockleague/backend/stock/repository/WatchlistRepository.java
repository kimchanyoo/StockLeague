package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Watchlist;
import com.stockleague.backend.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    Page<Watchlist> findAllByUser(User user, Pageable pageable);
}
