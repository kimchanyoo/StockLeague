package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.CommentLike;
import com.stockleague.backend.user.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    @Query("""
                SELECT cl.comment.id 
                FROM CommentLike cl 
                WHERE cl.user.id = :userId 
                AND cl.comment.id IN :commentIds 
                AND cl.isLiked = true
            """)
    List<Long> findLikedCommentIdsByUserIdAndCommentIds(
            @Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);
}
