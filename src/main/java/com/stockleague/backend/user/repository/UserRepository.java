package com.stockleague.backend.user.repository;

import com.stockleague.backend.user.domain.OauthServerType;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.dto.projection.UserIdAndNicknameProjection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthIdAndProvider(String oauthId, OauthServerType provider);

    boolean existsByNickname(String nickname);

    @Query("SELECT u.id FROM User u")
    List<Long> findAllUserIds();

    @Query("SELECT u.id AS id, u.nickname AS nickname FROM User u WHERE u.id IN :userIds")
    List<UserIdAndNicknameProjection> findIdAndNicknameByIds();


}
