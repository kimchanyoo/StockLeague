package com.stockleague.backend.inquiry.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.inquiry.domain.Inquiry;
import com.stockleague.backend.inquiry.domain.InquiryAnswer;
import com.stockleague.backend.inquiry.dto.request.InquiryAnswerCreateRequestDto;
import com.stockleague.backend.inquiry.dto.response.InquiryAnswerCreateResponseDto;
import com.stockleague.backend.inquiry.repository.InquiryAnswerRepository;
import com.stockleague.backend.inquiry.repository.InquiryRepository;
import com.stockleague.backend.notification.domain.NotificationType;
import com.stockleague.backend.notification.domain.TargetType;
import com.stockleague.backend.notification.dto.NotificationEvent;
import com.stockleague.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryAnswerService {

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;

    private final NotificationService notificationService;

    /**
     * 문의에 답변을 등록하고, 문의 작성자에게 알림을 발송합니다.
     * - 빈 본문이면 MISSING_FIELDS
     * - 문의가 없으면 INQUIRY_NOT_FOUND
     * - 알림 대상: inquiry.userId
     * - TargetType.INQUIRY, targetId = inquiryId
     * - 동일 사용자가 스스로에게 답변한 경우(드물지만) 알림 생략
     */
    @Transactional
    public InquiryAnswerCreateResponseDto createInquiryAnswer(
            InquiryAnswerCreateRequestDto request, Long answerUserId, Long inquiryId) {

        if (request.content().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.INQUIRY_NOT_FOUND));

        InquiryAnswer answer = InquiryAnswer.builder()
                .inquiry(inquiry)
                .userId(answerUserId)
                .content(request.content())
                .build();

        InquiryAnswer savedAnswer = inquiryAnswerRepository.save(answer);

        inquiry.markAsAnswered();

        Long ownerId = inquiry.getUserId();
        if (ownerId != null && !ownerId.equals(answerUserId)) {
            String overrideMsg = String.format("문의 \"%s\"에 답변이 등록되었습니다.", inquiry.getTitle());
            notificationService.notify(
                    new NotificationEvent(
                            ownerId,
                            NotificationType.INQUIRY_ANSWER,
                            TargetType.INQUIRY,
                            inquiry.getId()
                    ),
                    overrideMsg
            );
        }

        return InquiryAnswerCreateResponseDto.from(savedAnswer);
    }
}
