package com.stockleague.backend.stock.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.stock.dto.request.CommentReportListRequestDto;
import com.stockleague.backend.stock.dto.request.CommentReportRequestDto;
import com.stockleague.backend.stock.dto.response.CommentReportDetailResponseDto;
import com.stockleague.backend.stock.dto.response.CommentReportListResponseDto;
import com.stockleague.backend.stock.dto.response.CommentReportResponseDto;
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
                    content = @Content(schema = @Schema(implementation = CommentReportResponseDto.class),
                            examples = @ExampleObject(name = "ReportCreateSuccess",
                                    summary = "신고 완료",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "신고가 정상적으로 접수되었습니다.",
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
    public ResponseEntity<CommentReportResponseDto> createReport(
            @PathVariable Long targetId,
            @Valid @RequestBody CommentReportRequestDto request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        CommentReportResponseDto response = reportService.createReport(request, userId, targetId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/reports")
    @Operation(summary = "신고 목록 조회", description = "전체 신고 목록을 페이지네이션과 상태(Status) 필터로 조회합니다.")
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
                                                  "success": true
                                                  "reports": [
                                                    {
                                                      "reportId": 1,
                                                      "reason": "욕설 포함",
                                                      "status": "PENDING",
                                                      "createdAt": "2025-05-20T15:30:00"
                                                    }
                                                  ],
                                                  "page": 1,
                                                  "size": 10,
                                                  "totalElements": 1,
                                                  "totalPages": 1
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
            @Valid @RequestBody CommentReportListRequestDto request
    ) {
        CommentReportListResponseDto result = reportService.listReports(request, page, size);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/{reportId}")
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
                                              "message": "신고 내용 불러오기에 성공했습니다.",
                                              "reportId": 1,
                                              "targetType": "COMMENT",
                                              "targetId": 100,
                                              "reporterNickname": "신고자1",
                                              "processedByNickname": "관리자A",
                                              "reason": "욕설 포함",
                                              "additionalInfo": "지속적인 비방이 있습니다.",
                                              "status": "RESOLVED",
                                              "createdAt": "2025-05-20T15:30:00",
                                              "processedAt": "2025-05-21T10:15:00",
                                              "actionTaken": "DELETED"
                                            }
                                            """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해당 ID의 신고가 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ReportNotFound",
                                    summary = "해당 신고 정보가 존재하지 않을 경우",
                                    value = """
                                                {
                                                  "success": false,
                                                  "message": "해당 신고을 찾을 수 없습니다.",
                                                  "errorCode": "REPORT_NOT_FOUND"
                                                }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<CommentReportDetailResponseDto> getReportDetail(
            @PathVariable Long reportId
    ) {
        CommentReportDetailResponseDto result = reportService.getReport(reportId);

        return ResponseEntity.ok(result);
    }
}
