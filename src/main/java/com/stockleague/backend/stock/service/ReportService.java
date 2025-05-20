package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.domain.TargetType;
import com.stockleague.backend.stock.dto.request.CommentReportRequestDto;
import com.stockleague.backend.stock.dto.response.CommentReportResponseDto;
import com.stockleague.backend.stock.repository.CommentReportRepository;
import com.stockleague.backend.stock.repository.CommentRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
}
