package com.stockleague.backend.notice.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.notice.dto.request.NoticeCreateRequestDto;
import com.stockleague.backend.notice.dto.request.NoticeUpdateRequestDto;
import com.stockleague.backend.notice.dto.response.NoticeAdminPageResponseDto;
import com.stockleague.backend.notice.dto.response.NoticeCreateResponseDto;
import com.stockleague.backend.notice.dto.response.NoticeUpdateResponseDto;
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
@RequestMapping("/api/v1/admin/notices")
@Tag(name = "Notice(Admin)", description = "공지사항 관련 API(관리자용)")
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
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        NoticeCreateResponseDto responseDto = noticeService.createNotice(request, userId);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    @Operation(summary = "공지사항 목록 조회", description = "관리자가 볼 수 있는 공지사항 전체 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = NoticeAdminPageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지네이션 파라미터 오류",
                    content = @Content(mediaType = "application/json",
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
    public ResponseEntity<NoticeAdminPageResponseDto> getNoticeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isDeleted) {

        NoticeAdminPageResponseDto response = noticeService.getAdminNoticeList(page, size, isDeleted);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{noticeId}")
    @Operation(summary = "공지사항 수정", description = "공지사항의 제목, 내용, 고정 여부 등을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 수정 성공",
                    content = @Content(schema = @Schema(implementation = NoticeUpdateResponseDto.class),
                            examples = @ExampleObject(
                                    name = "UpdateSuccess",
                                    summary = "수정 성공",
                                    value = """
                                                {
                                                  "success": true,
                                                  "message": "공지사항이 성공적으로 수정되었습니다."
                                                }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "공지사항 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "NoticeNotFound",
                                    summary = "존재하지 않는 공지사항",
                                    value = """
                                                {
                                                  "success": false,
                                                  "message": "수정할 공지사항이 존재하지 않습니다.",
                                                  "errorCode": "NOTICE_NOT_FOUND"
                                                }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<NoticeUpdateResponseDto> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeUpdateRequestDto requestDto
    ) {
        NoticeUpdateResponseDto notice = noticeService.updateNotice(noticeId, requestDto);
        return ResponseEntity.ok(notice);
    }
}
