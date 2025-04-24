package com.stockleague.backend.inquiry.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.inquiry.domain.Inquiry;
import com.stockleague.backend.inquiry.domain.InquiryStatus;
import com.stockleague.backend.inquiry.dto.request.InquiryCreateRequestDto;
import com.stockleague.backend.inquiry.dto.response.InquiryCreateResponseDto;
import com.stockleague.backend.inquiry.repository.InquiryRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    public InquiryCreateResponseDto createInquiry(
            InquiryCreateRequestDto request, Long userId) {
        if (request.title().isBlank() || request.content().isBlank() || request.category().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        Inquiry inquiry = Inquiry.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .title(request.title())
                .category(request.category())
                .content(request.content())
                .status(InquiryStatus.WAITING)
                .build();

        Inquiry savedInquiry = inquiryRepository.save(inquiry);

        return new InquiryCreateResponseDto(
                true,
                "문의가 정상적으로 접수되었습니다.",
                savedInquiry.getId()
        );
    }
}
