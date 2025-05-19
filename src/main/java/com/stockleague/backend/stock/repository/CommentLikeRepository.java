package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.CommentLike;
import com.stockleague.backend.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    boolean existsByUserIdAndCommentIdAndIsLikedTrue(Long userId, Long commentId);

    Long user(User user);
}
