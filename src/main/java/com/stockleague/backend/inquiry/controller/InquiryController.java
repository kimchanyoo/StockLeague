package com.stockleague.backend.inquiry.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.inquiry.dto.request.InquiryCreateRequestDto;
import com.stockleague.backend.inquiry.dto.response.InquiryCreateResponseDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
@Tag(name = "Inquiry", description = "문의 관련 API")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    @Operation(summary = "1:1 문의 작성", description = "사용자가 고객센터에 1:1 문의를 작성 및 접수합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "문의 작성 성공",
                    content = @Content(
                            schema = @Schema(implementation = InquiryCreateResponseDto.class),
                            examples = @ExampleObject(
                                    name = "InquiryCreateSuccess",
                                    summary = "문의 등록 성공 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "문의가 정상적으로 접수되었습니다.",
                                              "inquiryId": 101
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
                                              "message": "문의 제목과 내용을 모두 입력해주세요.",
                                              "errorCode": "MISSING_FIELDS"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해당 사용자가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = org.springframework.web.ErrorResponse.class),
                            examples = @ExampleObject(
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
                    )
            )
    })
    public ResponseEntity<InquiryCreateResponseDto> createInquiry(
            @Valid @RequestBody InquiryCreateRequestDto request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        InquiryCreateResponseDto response = inquiryService.createInquiry(request, userId);

        return ResponseEntity.ok(response);
    }
}
