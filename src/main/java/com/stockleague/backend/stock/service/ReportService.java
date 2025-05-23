package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.dto.request.report.CommentReportListRequestDto;
import com.stockleague.backend.stock.dto.request.report.CommentReportRequestDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportDetailResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportListResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportSummaryDto;
import com.stockleague.backend.stock.dto.response.report.ReportDetailDto;
import com.stockleague.backend.stock.dto.response.report.WarningHistoryDto;
import com.stockleague.backend.stock.repository.CommentReportRepository;
import com.stockleague.backend.stock.repository.CommentRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import com.stockleague.backend.user.repository.UserWarningRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final CommentReportRepository commentReportRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserWarningRepository userWarningRepository;

    public CommentReportResponseDto createReport(CommentReportRequestDto request, Long userId, Long targetId) {

        if (request.reason() == null || request.additionalInfo().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        Comment comment = commentRepository.findById(targetId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        CommentReport commentReport = CommentReport.builder()
                .comment(comment)
                .reporter(user)
                .reason(request.reason())
                .additionalInfo(request.additionalInfo())
                .build();

        commentReportRepository.save(commentReport);
        comment.increaseReportCount();

        return CommentReportResponseDto.from();
    }

    public CommentReportListResponseDto listReports(CommentReportListRequestDto request, int page, int size) {

        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        Pageable pageable;
        Page<CommentReport> reportPage;

        if (request.status() != null) {
            pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createdAt")));
            reportPage = commentReportRepository.findByStatus(request.status(), pageable);
        } else {
            pageable = PageRequest.of(page - 1, size);
            reportPage = commentReportRepository.findAllOrderByWaitingFirst(pageable);
        }

        List<CommentReportSummaryDto> contents = reportPage.getContent().stream()
                .map(CommentReportSummaryDto::from)
                .toList();

        return new CommentReportListResponseDto(
                true,
                contents,
                page,
                size,
                reportPage.getTotalElements(),
                reportPage.getTotalPages()
        );
    }

    public CommentReportDetailResponseDto getReport(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        User user = comment.getUser();

        List<ReportDetailDto> reports = commentReportRepository.findAllByComment(comment).stream()
                .map(ReportDetailDto::from
                ).toList();

        List<WarningHistoryDto> warnings = userWarningRepository.findAllByWarnedUser(user).stream()
                .map(WarningHistoryDto::from
                ).toList();

        return new CommentReportDetailResponseDto(
                true,
                "신고 목록을 성공적으로 불러왔습니다.",
                comment.getId(),
                user.getNickname(),
                comment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                comment.getStock().getStockName(),
                user.getId(),
                user.getWarningCount(),
                user.getIsBanned(),
                reports,
                warnings
        );
    }
}
