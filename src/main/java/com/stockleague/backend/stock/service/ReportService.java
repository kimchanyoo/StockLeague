package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.notification.domain.NotificationType;
import com.stockleague.backend.notification.domain.TargetType;
import com.stockleague.backend.notification.dto.NotificationEvent;
import com.stockleague.backend.kafka.producer.NotificationProducer;
import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.domain.Status;
import com.stockleague.backend.stock.dto.request.report.CommentDeleteAdminRequestDto;
import com.stockleague.backend.stock.dto.request.report.CommentReportRequestDto;
import com.stockleague.backend.stock.dto.response.report.CommentDeleteAdminResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportDetailResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportListResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportRejectResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportSummaryDto;
import com.stockleague.backend.stock.dto.response.report.ReportDetailDto;
import com.stockleague.backend.stock.dto.response.report.WarningHistoryDto;
import com.stockleague.backend.stock.repository.CommentReportRepository;
import com.stockleague.backend.stock.repository.CommentRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserWarning;
import com.stockleague.backend.user.repository.UserRepository;
import com.stockleague.backend.user.repository.UserWarningRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final CommentReportRepository commentReportRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserWarningRepository userWarningRepository;
    private final NotificationProducer notificationProducer;

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

    public CommentReportListResponseDto listReports(Status status, int page, int size) {

        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        Pageable pageable;
        Page<CommentReport> reportPage;

        if (status != null) {
            pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createdAt")));
            reportPage = commentReportRepository.findByComment_Status(status, pageable);
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
                comment.getContent(),
                user.getId(),
                user.getWarningCount(),
                user.getIsBanned(),
                Optional.ofNullable(comment.getProcessedBy())
                        .map(User::getNickname)
                        .orElse(null),
                comment.getActionTaken(),
                comment.getStatus(),
                reports,
                warnings
        );
    }

    @Transactional
    public CommentDeleteAdminResponseDto deleteCommentAndWarn(
            CommentDeleteAdminRequestDto request, Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));
        User user = comment.getUser();

        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        comment.markDeletedAndWarnedByAdmin(admin);
        user.increaseWarningCount();

        userWarningRepository.save(UserWarning.builder()
                .warnedUser(user)
                .comment(comment)
                .admin(admin)
                .reason(request.reason())
                .build()
        );

        NotificationEvent event = new NotificationEvent(
                user.getId(),
                NotificationType.COMMENT_DELETED_AND_WARNED,
                TargetType.COMMENT,
                commentId
        );

        notificationProducer.send(event);

        return new CommentDeleteAdminResponseDto(
                true,
                "댓글이 삭제되고 경고가 부여되었습니다."
        );
    }

    @Transactional
    public CommentReportRejectResponseDto rejectReport(Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        comment.rejectByAdmin(admin);

        return CommentReportRejectResponseDto.from();
    }
}
