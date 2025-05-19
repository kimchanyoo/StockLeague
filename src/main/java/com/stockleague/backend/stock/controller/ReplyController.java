package com.stockleague.backend.stock.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.stock.dto.request.ReplyCreateRequestDto;
import com.stockleague.backend.stock.dto.request.ReplyUpdateRequestDto;
import com.stockleague.backend.stock.dto.response.ReplyCreateResponseDto;
import com.stockleague.backend.stock.dto.response.ReplyUpdateResponseDto;
import com.stockleague.backend.stock.service.ReplyService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
@Tag(name = "Reply", description = "주식 대댓글 관련 API")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/{ticker}/comments/{commentId}/reply")
    @Operation(summary = "대댓글 작성", description = "특정 댓글에 대댓글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "대댓글 작성 성공",
                    content = @Content(schema = @Schema(implementation = ReplyCreateResponseDto.class),
                            examples = @ExampleObject(
                                    name = "ReplyCreateSuccess",
                                    summary = "댓글 등록 완료",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "답글이 등록되었습니다.",
                                              "replyId": 456,
                                              "createdAt": "2025-03-18T18:00:00Z",
                                              "nickname": "스톡마스터"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 데이터 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "MissingFields",
                                    summary = "필수 입력값 누락",
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
            @ApiResponse(responseCode = "404", description = "종목 정보 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "StockNotFound",
                                    summary = "해당 티커에 대한 주식 정보가 존재하지 않을 경우",
                                    value = """
                                                {
                                                  "success": false,
                                                  "message": "해당 종목을 찾을 수 없습니다.",
                                                  "errorCode": "STOCK_NOT_FOUND"
                                                }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해당 사용자가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
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
    public ResponseEntity<ReplyCreateResponseDto> createReply(
            @PathVariable String ticker,
            @PathVariable Long commentId,
            @RequestBody @Valid ReplyCreateRequestDto request,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        ReplyCreateResponseDto result = replyService.createReply(request, ticker, commentId, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/comment/replies/{replyId}")
    @Operation(summary = "대댓글 수정", description = "자신이 작성한 대댓글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대댓글 수정 성공",
                    content = @Content(schema = @Schema(implementation = ReplyUpdateResponseDto.class),
                            examples = @ExampleObject(
                                    name = "ReplyUpdateSuccess",
                                    summary = "대댓글 수정 완료",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "대댓글이 성공적으로 수정되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 데이터 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "MissingFields",
                                    summary = "필수 입력값 누락",
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
            @ApiResponse(responseCode = "403", description = "댓글 수정 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidCommentOwner",
                                    summary = "자신이 작성하지 않은 댓글을 수정하려는 경우",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "자신이 작성한 댓글만 수정할 수 있습니다.",
                                              "errorCode": "INVALID_COMMENT_OWNER"
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
            ),
    })
    public ResponseEntity<ReplyUpdateResponseDto> updateReply(
            @Valid @RequestBody ReplyUpdateRequestDto request,
            @PathVariable Long replyId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        ReplyUpdateResponseDto result = replyService.updateReply(request, replyId, userId);

        return ResponseEntity.ok(result);
    }
}
