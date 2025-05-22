package com.stockleague.backend.notice.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.notice.dto.response.NoticeDetailResponseDto;
import com.stockleague.backend.notice.dto.response.NoticePageResponseDto;
import com.stockleague.backend.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
@Tag(name = "Notice", description = "공지사항 관련 API")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/search")
    @Operation(summary = "공지사항 검색", description = "제목 또는 내용에서 키워드를 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = NoticePageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "검색어 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "MissingKeyword",
                                            summary = "키워드 없음",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "검색어를 입력해주세요.",
                                                      "errorCode": "MISSING_KEYWORD"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
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
                            }
                    )
            )
    })
    public ResponseEntity<NoticePageResponseDto> searchNotice(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new GlobalException(GlobalErrorCode.MISSING_KEYWORD);
        }

        NoticePageResponseDto response = noticeService.search(keyword, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "공지사항 목록 조회", description = "유저가 볼 수 있는 공지사항 전체 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = NoticePageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지네이션 파라미터 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
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
    public ResponseEntity<NoticePageResponseDto> getNoticeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        NoticePageResponseDto response = noticeService.getNoticeList(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = NoticeDetailResponseDto.class),
                            examples = @ExampleObject(
                                    name = "NoticeDetailSuccess",
                                    summary = "상세 공지 조회 성공",
                                    value = """
                                            {
                                              "success": true,
                                              "noticeId": 101,
                                              "title": "시스템 점검 안내",
                                              "category": "점검",
                                              "content": "3월 20일 오전 2시부터 4시까지 시스템 점검이 진행됩니다.",
                                              "createdAt": "2025-03-18T08:00:00Z",
                                              "isPinned": false
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "공지사항 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "NoticeNotFound",
                                    summary = "공지 없음",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "해당 공지사항을 찾을 수 없습니다.",
                                              "errorCode": "NOTICE_NOT_FOUND"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<NoticeDetailResponseDto> getNoticeDetail(@PathVariable Long noticeId) {
        NoticeDetailResponseDto response = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity.ok(response);
    }

}
