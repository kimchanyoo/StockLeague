package com.stockleague.backend.user.repository;

import com.stockleague.backend.user.domain.UserAsset;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserAssetRepository extends JpaRepository<UserAsset, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ua from UserAsset ua where ua.userId = :userId")
    Optional<UserAsset> findByUserIdForUpdate(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserAsset ua where ua.userId = :userId")
    int bulkDeleteByUserId(Long userId);
}
