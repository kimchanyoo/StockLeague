package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.domain.TargetType;
import com.stockleague.backend.stock.dto.request.CommentReportListRequestDto;
import com.stockleague.backend.stock.dto.request.CommentReportRequestDto;
import com.stockleague.backend.stock.dto.response.CommentReportDetailResponseDto;
import com.stockleague.backend.stock.dto.response.CommentReportListResponseDto;
import com.stockleague.backend.stock.dto.response.CommentReportResponseDto;
import com.stockleague.backend.stock.dto.response.CommentReportSummaryDto;
import com.stockleague.backend.stock.repository.CommentReportRepository;
import com.stockleague.backend.stock.repository.CommentRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
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

    public CommentReportResponseDto createReport(CommentReportRequestDto request, Long userId, Long targetId) {

        if(request.reason().isBlank() || request.additionalInfo().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        Comment comment = commentRepository.findById(targetId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        CommentReport commentReport = CommentReport.builder()
                .targetType(comment.getParent() != null ? TargetType.REPLY : TargetType.COMMENT)
                .target(comment)
                .reporter(user)
                .reason(request.reason())
                .additionalInfo(request.additionalInfo())
                .build();

        commentReportRepository.save(commentReport);

        return CommentReportResponseDto.from();
    }

    public CommentReportListResponseDto listReports(CommentReportListRequestDto request, int page, int size) {

        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createdAt")));


        Page<CommentReport> reportPage;

        if (request.status() != null) {
            reportPage = commentReportRepository.findByStatus(request.status(), pageable);
        } else {
            reportPage = commentReportRepository.findAll(pageable);
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

    public CommentReportDetailResponseDto getReport(Long reportId) {

        CommentReport report = commentReportRepository.findByReportId(reportId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.REPORT_NOT_FOUND));

        return CommentReportDetailResponseDto.from(report);
    }
}
