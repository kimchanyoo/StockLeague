package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.domain.Status;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentReportRepository extends JpaRepository<CommentReport, Integer> {

    Page<CommentReport> findByStatus(Status status, Pageable pageable);

    Page<CommentReport> findAll(Pageable pageable);

    Optional<CommentReport> findByReportId(Long reportId);
}
