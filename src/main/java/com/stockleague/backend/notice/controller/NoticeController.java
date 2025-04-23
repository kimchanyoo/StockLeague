package com.stockleague.backend.notice.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.notice.dto.response.NoticeSearchResponseDto;
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
                    content = @Content(schema = @Schema(implementation = NoticeSearchResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "검색어 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "MissingKeyword",
                                    summary = "키워드 없음",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "검색어를 입력해주세요.",
                                              "errorCode": "MISSING_KEYWORD"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<NoticeSearchResponseDto> searchNotice(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new GlobalException(GlobalErrorCode.MISSING_KEYWORD);
        }

        NoticeSearchResponseDto response = noticeService.search(keyword, page, size);
        return ResponseEntity.ok(response);
    }
}
