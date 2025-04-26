package com.stockleague.backend.inquiry.repository;

import com.stockleague.backend.inquiry.domain.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {
}
