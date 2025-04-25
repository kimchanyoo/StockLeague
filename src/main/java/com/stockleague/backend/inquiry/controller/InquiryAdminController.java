package com.stockleague.backend.inquiry.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.inquiry.dto.response.InquiryPageResponseDto;
import com.stockleague.backend.inquiry.service.InquiryService;
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
@RequestMapping("/api/v1/admin/inquiries")
@Tag(name = "Inquiry(Admin)", description = "문의 관련 API(관리자용)")
public class InquiryAdminController {

    private final InquiryService inquiryService;

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
                                                  "createdAt": "2025-04-24T08:19:43"
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
}
