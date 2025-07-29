package com.stockleague.backend.user.repository;

import com.stockleague.backend.user.domain.UserAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAssetRepository extends JpaRepository<UserAsset, Long> {
}
