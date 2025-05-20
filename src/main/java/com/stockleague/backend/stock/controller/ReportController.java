package com.stockleague.backend.stock.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.stock.dto.request.CommentReportRequestDto;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
                    content = @Content(schema = @Schema(implementation = CommentReportResponseDto.class))),
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
}
