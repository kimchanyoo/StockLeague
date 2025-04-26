package com.stockleague.backend.inquiry.repository;

import com.stockleague.backend.inquiry.domain.Inquiry;
import com.stockleague.backend.inquiry.domain.InquiryStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Page<Inquiry> findByUserId(Long userId, Pageable pageable);

    Page<Inquiry> findByUserIdAndStatus(Long userId, InquiryStatus status, Pageable pageable);

    Page<Inquiry> findByStatus(InquiryStatus status, Pageable pageable);

    Optional<Inquiry> findByUserIdAndId(Long userId, Long inquiryId);
}
