package com.stockleague.backend.stock.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.stock.domain.Status;
import com.stockleague.backend.stock.dto.request.report.CommentDeleteAdminRequestDto;
import com.stockleague.backend.stock.dto.request.report.CommentReportCreateRequestDto;
import com.stockleague.backend.stock.dto.response.report.CommentDeleteAdminResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportDetailResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportListResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportRejectResponseDto;
import com.stockleague.backend.stock.dto.response.report.CommentReportCreateResponseDto;
import com.stockleague.backend.stock.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Report", description = "주식 신고 관련 API")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/reports/{targetId}")
    @Operation(summary = "댓글/대댓글 신고", description = "댓글 또는 대댓글을 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "신고 성공",
                    content = @Content(schema = @Schema(implementation = CommentReportCreateResponseDto.class),
                            examples = @ExampleObject(name = "ReportCreateSuccess",
                                    summary = "신고 완료",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "신고가 정상적으로 접수되었습니다."
                                            }
                                            """
                            ))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Missing Fields",
                                    value = """
                                                {
                                                  "success": false,
                                                  "message": "필수 입력값이 누락되었습니다.",
                                                  "errorCode": "MISSING_FIELDS"
                                                }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "종목 정보 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "UserNotFound",
                                            summary = "존재하지 않는 사용자",
                                            value = """
                                                    {
                                                        "success" : false,
                                                        "message" : "해당 사용자를 찾을 수 없습니다.",
                                                        "errorCode": "USER_NOT_FOUND"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "CommentNotFound",
                                            summary = "해당 댓글 정보가 존재하지 않을 경우",
                                            value = """
                                                        {
                                                          "success": false,
                                                          "message": "해당 댓글을 찾을 수 없습니다.",
                                                          "errorCode": "COMMENT_NOT_FOUND"
                                                        }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<CommentReportCreateResponseDto> createReport(
            @PathVariable Long targetId,
            @Valid @RequestBody CommentReportCreateRequestDto request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        CommentReportCreateResponseDto response = reportService.createReport(request, userId, targetId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/reports")
    @Operation(summary = "신고 목록 조회", description = "전체 조회 및 상태별 조회 시 공통 형식")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentReportListResponseDto.class),
                            examples = @ExampleObject(
                                    name = "CommentReportListSuccess",
                                    summary = "신고 목록 조회",
                                    value = """
                                                {
                                                   "success": true,
                                                   "reports": [
                                                     {
                                                       "commentId": 123,
                                                       "authorNickname": "신고자1",
                                                       "reportCount": 2,
                                                       "warningCount": 1,
                                                       "status": "WAITING"
                                                     },
                                                     {
                                                       "commentId": 124,
                                                       "authorNickname": "신고자2",
                                                       "reportCount": 1,
                                                       "warningCount": 0,
                                                       "status": "RESOLVED"
                                                     }
                                                   ],
                                                   "page": 1,
                                                   "size": 10,
                                                   "totalCount": 2,
                                                   "totalPage": 1
                                                 }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidPagination",
                                    summary = "잘못된 페이지네이션 파라미터",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "페이지 번호 또는 크기가 유효하지 않습니다.",
                                              "errorCode": "INVALID_PAGINATION"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<CommentReportListResponseDto> getListReport(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Status status
    ) {
        CommentReportListResponseDto result = reportService.listReports(status, page, size);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/reports/{commentId}")
    @Operation(summary = "신고 상세 조회", description = "신고 ID를 기반으로 신고 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentReportDetailResponseDto.class),
                            examples = @ExampleObject(
                                    name = "ReportDetailSuccess",
                                    summary = "신고 조회 성공",
                                    value = """
                                            {
                                               "success": true,
                                               "message": "신고 목록을 성공적으로 불러왔습니다.",
                                               "commentId": 123,
                                               "commentAuthorNickname": "stockguru",
                                               "commentCreatedAt": "2025-05-23T14:03:00",
                                               "stockName": "삼성전자",
                                               "commentContent": "이게 주식이냐? XX",
                                               "commentAuthorId": 42,
                                               "warningCount": 2,
                                               "accountStatus" : false,
                                               "AdminNickname" : "관리자 A씨",
                                               "actionTaken" : "COMMENT_DELETED",
                                               "status" : "RESOLVED",
                                               "reports": [
                                                 {
                                                   "reporterNickname": "reporter01",
                                                   "reason": "INSULT",
                                                   "additionalInfo": "욕설이 포함되어 있습니다.",
                                                   "reportedAt": "2025-05-22T18:12:34"
                                                 },
                                                 {
                                                   "reporterNickname": "reporter02",
                                                   "reason": "SPAM",
                                                   "additionalInfo": "동일한 문장을 반복합니다.",
                                                   "reportedAt": "2025-05-22T19:05:10"
                                                 }
                                               ],
                                               "warnings": [
                                                 {
                                                   "warningAt": "2025-05-22T19:30:00",
                                                   "reason": "INSULT",
                                                   "commentId": 123,
                                                   "adminNickname": "관리자김씨"
                                                 },
                                                 {
                                                   "warningAt": "2025-04-01T12:45:00",
                                                   "reason": "SPAM",
                                                   "commentId": 87,
                                                   "adminNickname": "관리자박씨"
                                                 }
                                               ]
                                             }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "댓글이 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CommentNotFound",
                                    summary = "해당 댓글 정보가 존재하지 않을 경우",
                                    value = """
                                                {
                                                  "success": false,
                                                  "message": "해당 댓글을 찾을 수 없습니다.",
                                                  "errorCode": "COMMENT_NOT_FOUND"
                                                }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<CommentReportDetailResponseDto> getReportDetail(
            @PathVariable Long commentId
    ) {
        CommentReportDetailResponseDto result = reportService.getReport(commentId);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/admin/comments/{commentId}/delete-with-warning")
    @Operation(
            summary = "댓글 삭제 및 경고 부여 (관리자 전용)",
            description = "관리자가 특정 댓글을 삭제하고, 작성자에게 경고를 부여합니다. 알림이 발송되고, 경고 이력이 저장됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 삭제 및 경고 성공",
                    content = @Content(schema = @Schema(implementation = CommentDeleteAdminResponseDto.class),
                            examples = @ExampleObject(name = "DeleteAndWarningSuccess",
                                    summary = "댓글 삭제 및 경고 처리 완료",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "댓글이 삭제되고 경고가 부여되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 정보 요청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "CommentNotFound",
                                            summary = "해당 댓글 정보가 존재하지 않을 경우",
                                            value = """
                                                        {
                                                          "success": false,
                                                          "message": "해당 댓글을 찾을 수 없습니다.",
                                                          "errorCode": "COMMENT_NOT_FOUND"
                                                        }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "UserNotFound",
                                            summary = "존재하지 않는 사용자",
                                            value = """
                                                    {
                                                        "success" : false,
                                                        "message" : "해당 사용자를 찾을 수 없습니다.",
                                                        "errorCode": "USER_NOT_FOUND"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<CommentDeleteAdminResponseDto> deleteCommentAndWarn(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentDeleteAdminRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();

        CommentDeleteAdminResponseDto result
                = reportService.deleteCommentAndWarn(request, commentId, adminId);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("admin/reports/{commentId}/reject")
    @Operation(summary = "댓글 신고 반려", description = "관리자가 댓글에 대한 신고를 반려 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 반려 성공",
                    content = @Content(schema = @Schema(implementation = CommentReportRejectResponseDto.class),
                            examples = @ExampleObject(name = "ReportRejectSuccess",
                                    summary = "신고 반려 성공",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "신고가 정상적으로 반려되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 정보 요청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "CommentNotFound",
                                            summary = "해당 댓글 정보가 존재하지 않을 경우",
                                            value = """
                                                        {
                                                          "success": false,
                                                          "message": "해당 댓글을 찾을 수 없습니다.",
                                                          "errorCode": "COMMENT_NOT_FOUND"
                                                        }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "UserNotFound",
                                            summary = "존재하지 않는 사용자",
                                            value = """
                                                    {
                                                        "success" : false,
                                                        "message" : "해당 사용자를 찾을 수 없습니다.",
                                                        "errorCode": "USER_NOT_FOUND"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<CommentReportRejectResponseDto> rejectReport(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();

        CommentReportRejectResponseDto response = reportService.rejectReport(commentId, adminId);
        return ResponseEntity.ok(response);
    }
}
