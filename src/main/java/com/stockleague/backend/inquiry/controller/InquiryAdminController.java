package com.stockleague.backend.inquiry.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.inquiry.dto.request.InquiryAnswerCreateRequestDto;
import com.stockleague.backend.inquiry.dto.response.InquiryAnswerCreateResponseDto;
import com.stockleague.backend.inquiry.dto.response.InquiryCreateResponseDto;
import com.stockleague.backend.inquiry.dto.response.InquiryDetailForAdminResponseDto;
import com.stockleague.backend.inquiry.dto.response.InquiryPageResponseDto;
import com.stockleague.backend.inquiry.service.InquiryAnswerService;
import com.stockleague.backend.inquiry.service.InquiryService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/inquiries")
@Tag(name = "Inquiry(Admin)", description = "문의 관련 API(관리자용)")
public class InquiryAdminController {

    private final InquiryService inquiryService;
    private final InquiryAnswerService inquiryAnswerService;

    @GetMapping
    @Operation(summary = "문의사항 조회(관리자용)", description = "관리자가 전체 1:1문의 내역을 조회하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의사항 조회 성공",
                    content = @Content(schema = @Schema(implementation = InquiryPageResponseDto.class),
                            examples = @ExampleObject(
                                    name = "InquiryListSuccess",
                                    summary = "문의 사항 조회 성공",
                                    value = """
                                            {
                                              "success": true,
                                              "inquiries": [
                                                {
                                                  "inquiryId": 102,
                                                  "userNickname": "테스트",
                                                  "category": "기술적 결함",
                                                  "title": "문의 테스트",
                                                  "status": "WAITING",
                                                  "createdAt": "2025-04-24T08:19:43",
                                                  "updatedAt": "2025-04-24T08:19:43",
                                                }
                                              ],
                                              "page": 1,
                                              "size": 10,
                                              "totalCount": 134
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400",
                    description = "페이지네이션 파라미터 오류",
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
    public ResponseEntity<InquiryPageResponseDto> inquiryForAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        InquiryPageResponseDto result = inquiryService.getInquiriesForAdmin(page, size, status);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{inquiryId}")
    @Operation(summary = "문의사항 상세 정보 조회(관리자용)", description = "관리자가 1:1문의 상세 정보를 조회하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의사항 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = InquiryDetailForAdminResponseDto.class),
                            examples = @ExampleObject(
                                    name = "InquiryDetailSuccess",
                                    summary = "문의 상세 조회 성공",
                                    value = """
                                            {
                                              "success": true,
                                              "inquiryId": 1002,
                                              "userId": "user01",
                                              "userNickname": "스탁유저",
                                              "title": "거래 오류 관련 문의",
                                              "category": "거래",
                                              "content": "매수가 정상적으로 안돼요",
                                              "status": "ANSWERED",
                                              "createdAt": "2025-03-18T14:00:00Z",
                                              "updatedAt": "2025-03-18T15:00:00Z",
                                              "answer": {
                                                "answerId": 2,
                                                "userId": 999,
                                                "content": "확인하겠습니다.",
                                                "answeredAt": "2025-03-18T14:15:00Z"
                                              }
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
            ),
            @ApiResponse(responseCode = "404", description = "공지사항 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InquiryNotFound",
                                    summary = "존재하지 않는 문의",
                                    value = """
                                                {
                                                  "success": false,
                                                  "message": "해당 문의를 찾을 수 없습니다.",
                                                  "errorCode": "INQUIRY_NOT_FOUND"
                                                }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<InquiryDetailForAdminResponseDto> getInquiryDetailForAdmin(
            @PathVariable Long inquiryId
    ) {
        InquiryDetailForAdminResponseDto result = inquiryService.getInquiryDetailForAdmin(inquiryId);

        return ResponseEntity.ok(result);
    }

    @PostMapping("{inquiryId}/answers")
    @Operation(summary = "문의 답변", description = "관리자가 문의 답변을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "문의 답변 완료",
                    content = @Content(
                            schema = @Schema(implementation = InquiryCreateResponseDto.class),
                            examples = @ExampleObject(
                                    name = "InquiryAnswerCreateSuccess",
                                    summary = "문의 답변 등록 성공 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "답변이 등록되었습니다.",
                                              "answerId": 456,
                                              "status": "ANSWERED"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력 필드 누락",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "MissingFields",
                                    summary = "제목 또는 내용 누락",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "내용을 모두 입력해야 합니다.",
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
            ),
            @ApiResponse(responseCode = "404", description = "공지사항 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InquiryNotFound",
                                    summary = "존재하지 않는 문의",
                                    value = """
                                                {
                                                  "success": false,
                                                  "message": "해당 문의를 찾을 수 없습니다.",
                                                  "errorCode": "INQUIRY_NOT_FOUND"
                                                }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<InquiryAnswerCreateResponseDto> createInquiryAnswer(
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryAnswerCreateRequestDto request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        InquiryAnswerCreateResponseDto result =
                inquiryAnswerService.createInquiryAnswer(request, userId, inquiryId);

        return ResponseEntity.ok(result);
    }
}
