package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.domain.Status;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentReportRepository extends JpaRepository<CommentReport, Integer> {

    Page<CommentReport> findByStatus(Status status, Pageable pageable);

    @Query("""
        SELECT r
        FROM CommentReport r
        ORDER BY
            CASE
                WHEN r.status = 'WAITING' THEN 0
                ELSE 1
            END,
            r.createdAt DESC
        """)
    Page<CommentReport> findAllOrderByWaitingFirst(Pageable pageable);

    List<CommentReport> findAllByComment(Comment comment);
}
