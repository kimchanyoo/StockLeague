package com.stockleague.backend.notice.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.notice.domain.Notice;
import com.stockleague.backend.notice.dto.request.NoticeCreateRequestDto;
import com.stockleague.backend.notice.dto.request.NoticeUpdateRequestDto;
import com.stockleague.backend.notice.dto.response.NoticeAdminPageResponseDto;
import com.stockleague.backend.notice.dto.response.NoticeAdminSummaryDto;
import com.stockleague.backend.notice.dto.response.NoticeCreateResponseDto;
import com.stockleague.backend.notice.dto.response.NoticeDetailResponseDto;
import com.stockleague.backend.notice.dto.response.NoticePageResponseDto;
import com.stockleague.backend.notice.dto.response.NoticeSummaryDto;
import com.stockleague.backend.notice.dto.response.NoticeUpdateResponseDto;
import com.stockleague.backend.notice.repository.NoticeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeCreateResponseDto createNotice(NoticeCreateRequestDto request, Long userId) {

        if (request.title().isBlank() || request.content().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        Notice notice = Notice.builder()
                .title(request.title())
                .category(request.category())
                .content(request.content())
                .isPinned(false)
                .userId(userId)
                .build();

        Notice savedNotice = noticeRepository.save(notice);

        return new NoticeCreateResponseDto(
                true,
                "공지사항이 등록되었습니다.",
                savedNotice.getId()
        );
    }

    public NoticePageResponseDto search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Notice> result = noticeRepository.findByTitleContainingOrContentContainingAndDeletedAtIsNull(
                keyword, keyword, pageable
        );

        List<NoticeSummaryDto> notices = result.getContent().stream()
                .map(NoticeSummaryDto::from)
                .toList();

        return new NoticePageResponseDto(true, notices, page, size, result.getTotalElements());
    }

    public NoticePageResponseDto getNoticeList(int page, int size) {

        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(
                Sort.Order.desc("isPinned"),
                Sort.Order.desc("createdAt")
        ));
        Page<Notice> noticePage = noticeRepository.findByDeletedAtIsNull(pageable);

        List<NoticeSummaryDto> noticeList = noticePage.getContent().stream()
                .map(NoticeSummaryDto::from)
                .toList();

        return new NoticePageResponseDto(true, noticeList, page, size, noticePage.getTotalElements());
    }

    public NoticeAdminPageResponseDto getAdminNoticeList(int page, int size, Boolean isDeleted) {

        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(
                Sort.Order.desc("isPinned"),
                Sort.Order.desc("createdAt")
        ));

        Page<Notice> noticePage;

        if (isDeleted == null) {
            noticePage = noticeRepository.findAll(pageable);
        } else if (isDeleted) {
            noticePage = noticeRepository.findByDeletedAtIsNotNull(pageable);
        } else {
            noticePage = noticeRepository.findByDeletedAtIsNull(pageable);
        }

        List<NoticeAdminSummaryDto> noticeAdminList = noticePage.getContent().stream()
                .map(NoticeAdminSummaryDto::from)
                .toList();

        return new NoticeAdminPageResponseDto(true, noticeAdminList, page, size, noticePage.getTotalElements());
    }

    public NoticeDetailResponseDto getNoticeDetail(long noticeId) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(noticeId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOTICE_NOT_FOUND));

        return NoticeDetailResponseDto.from(notice);
    }

    @Transactional
    public NoticeUpdateResponseDto updateNotice(Long noticeId, NoticeUpdateRequestDto request) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(noticeId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOTICE_NOT_FOUND));

        if (request.title() != null) {
            notice.updateTitle(request.title());
        }

        if (request.content() != null) {
            notice.updateContent(request.content());
        }

        if (request.category() != null) {
            notice.updateCategory(request.category());
        }

        if (request.isPinned() != null) {
            notice.updateIsPinned(request.isPinned());
        }

        return new NoticeUpdateResponseDto(true, "공지사항이 성공적으로 수정되었습니다.");
    }
}
