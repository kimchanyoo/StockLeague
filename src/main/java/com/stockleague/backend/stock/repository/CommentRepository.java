package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Comment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByParentId(Long parentId);

    Page<Comment> findByStockIdAndParentIsNull(Long stockId, Pageable pageable);
}
