package com.stockleague.backend.inquiry.repository;

import com.stockleague.backend.inquiry.domain.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
}
