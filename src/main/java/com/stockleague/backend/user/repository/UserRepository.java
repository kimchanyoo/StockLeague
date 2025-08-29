package com.stockleague.backend.user.repository;

import com.stockleague.backend.user.domain.OauthServerType;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.dto.projection.UserIdAndNicknameProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthIdAndProvider(String oauthId, OauthServerType provider);

    boolean existsByNickname(String nickname);

    @Query("select u.id as id, u.nickname as nickname from User u")
    List<UserIdAndNicknameProjection> findIdAndNickname();

    long countByCreatedAtAfter(LocalDateTime dateTime);

    long countByIsBannedFalse();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM users WHERE user_id = :id", nativeQuery = true)
    int hardDeleteById(@Param("id") Long id);
}
