package com.stockleague.backend.notice.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.notice.dto.request.NoticeCreateRequestDto;
import com.stockleague.backend.notice.dto.response.NoticeCreateResponseDto;
import com.stockleague.backend.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/notices")
@Tag(name = "Notice", description = "공지사항 관련 API(관리자용)")
public class NoticeAdminController {

    private final NoticeService noticeService;

    @PostMapping
    @Operation(summary = "공지사항 작성", description = "관리자가 새로운 공지사항을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "공지사항 작성 성공",
                    content = @Content(schema = @Schema(implementation = NoticeCreateResponseDto.class),
                            examples = @ExampleObject(
                                    name = "NoticeCreateSuccess",
                                    summary = "공지 등록 완료",
                                    value = """
                                        {
                                          "success": true,
                                          "message": "공지사항이 등록되었습니다.",
                                          "noticeId": 123
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "제목 또는 내용 누락",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "MissingFields",
                                    summary = "필수 입력값 누락",
                                    value = """
                                        {
                                          "success": false,
                                          "message": "제목과 내용은 모두 입력해야 합니다.",
                                          "errorCode": "MISSING_FIELDS"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "NoAdminPermission",
                                    summary = "관리자 권한 없음",
                                    value = """
                                        {
                                          "success": false,
                                          "message": "관리자 권한이 필요합니다.",
                                          "errorCode": "ACCESS_DENIED"
                                        }
                                        """
                            )
                    )
            )
    })
    public ResponseEntity<NoticeCreateResponseDto> createNotice(
            @Valid @RequestBody NoticeCreateRequestDto request,
            Authentication authentication){
        Long userId = (Long) authentication.getPrincipal();
        NoticeCreateResponseDto responseDto = noticeService.createNotice(request, userId);

        return ResponseEntity.ok(responseDto);
    }

}
