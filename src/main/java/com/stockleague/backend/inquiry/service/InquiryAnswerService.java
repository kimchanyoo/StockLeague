package com.stockleague.backend.inquiry.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.inquiry.domain.Inquiry;
import com.stockleague.backend.inquiry.domain.InquiryAnswer;
import com.stockleague.backend.inquiry.dto.request.InquiryAnswerCreateRequestDto;
import com.stockleague.backend.inquiry.dto.response.InquiryAnswerCreateResponseDto;
import com.stockleague.backend.inquiry.repository.InquiryAnswerRepository;
import com.stockleague.backend.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryAnswerService {

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;

    @Transactional
    public InquiryAnswerCreateResponseDto createInquiryAnswer(
            InquiryAnswerCreateRequestDto request, Long userId, Long inquiryId) {
        if (request.content().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.INQUIRY_NOT_FOUND));

        InquiryAnswer answer = InquiryAnswer.builder()
                .inquiry(inquiry)
                .userId(userId)
                .content(request.content())
                .build();

        InquiryAnswer savedAnswer = inquiryAnswerRepository.save(answer);

        inquiry.markAsAnswered();

        return InquiryAnswerCreateResponseDto.from(savedAnswer);
    }
}
