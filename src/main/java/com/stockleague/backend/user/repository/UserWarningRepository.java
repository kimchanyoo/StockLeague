package com.stockleague.backend.user.repository;

import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserWarning;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWarningRepository extends JpaRepository<UserWarning, Long> {

    List<UserWarning> findAllByWarnedUser(User user);
}
