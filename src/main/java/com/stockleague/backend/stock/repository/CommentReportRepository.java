package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentReportRepository extends JpaRepository<CommentReport, Integer> {
}
