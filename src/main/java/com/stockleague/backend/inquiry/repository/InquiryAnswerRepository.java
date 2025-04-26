package com.stockleague.backend.inquiry.repository;

import com.stockleague.backend.inquiry.domain.InquiryAnswer;
import org.springframework.data.repository.CrudRepository;

public interface InquiryAnswerRepository extends CrudRepository<InquiryAnswer, Integer> {
}
