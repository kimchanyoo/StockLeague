package com.stockleague.backend.user.repository;

import com.stockleague.backend.user.domain.OauthServerType;
import com.stockleague.backend.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthIdAndProvider(String oauthId, OauthServerType provider);

    boolean existsByNickname(String nickname);
}
