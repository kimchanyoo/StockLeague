package com.stockleague.backend.notice.repository;

import com.stockleague.backend.notice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByTitleContainingOrContentContainingAndDeletedAtIsNull(
            String title, String content, Pageable pageable);

    Page<Notice> findByDeletedAtIsNull(Pageable pageable);

    Page<Notice> findByDeletedAtIsNotNull(Pageable pageable);
}
