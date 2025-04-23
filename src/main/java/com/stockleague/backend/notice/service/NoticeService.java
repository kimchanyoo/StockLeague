package com.stockleague.backend.notice.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.notice.domain.Notice;
import com.stockleague.backend.notice.dto.request.NoticeCreateRequestDto;
import com.stockleague.backend.notice.dto.response.NoticeCreateResponseDto;
import com.stockleague.backend.notice.dto.response.NoticePageResponseDto;
import com.stockleague.backend.notice.dto.response.NoticeSummaryDto;
import com.stockleague.backend.notice.repository.NoticeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
}
