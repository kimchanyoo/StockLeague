package com.stockleague.backend.stock.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.stock.dto.request.ReplyCreateRequestDto;
import com.stockleague.backend.stock.dto.request.ReplyUpdateRequestDto;
import com.stockleague.backend.stock.dto.response.ReplyCreateResponseDto;
import com.stockleague.backend.stock.dto.response.ReplyDeleteResponseDto;
import com.stockleague.backend.stock.dto.response.ReplyListResponseDto;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Reply", description = "주식 대댓글 관련 API")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/{ticker}/comments/{commentId}/replies")
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

    @PatchMapping("/replies/{replyId}")
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
                                              "message": "자신이 작성한 댓글만 관리할 수 있습니다.",
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

    @DeleteMapping("/replies/{replyId}")
    @Operation(summary = "대댓글 삭제", description = "사용자가 자신이 작성한 대댓글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대댓글 삭제 성공",
                    content = @Content(schema = @Schema(implementation = ReplyDeleteResponseDto.class),
                            examples = @ExampleObject(
                                    name = "ReplyDeleteSuccess",
                                    summary = "대댓글 삭제 성공",
                                    value = """
                                                {
                                                  "success": true,
                                                  "message": "대댓글이 삭제되었습니다."
                                                }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidCommentOwner",
                                    summary = "자신이 작성하지 않은 댓글을 삭제하려는 경우",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "자신이 작성한 댓글만 관리할 수 있습니다.",
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
            )
    })
    public ResponseEntity<ReplyDeleteResponseDto> deleteReply(
            @PathVariable Long replyId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        ReplyDeleteResponseDto result = replyService.deleteReply(replyId, userId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/comments/{commentId}/replies")
    @Operation(summary = "대댓글 목록 조회", description = "특정 댓글에 작성된 모든 대댓글을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대댓글 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReplyListResponseDto.class),
                            examples = @ExampleObject(
                                    name = "ReplyListSuccess",
                                    summary = "대댓글 목록 반환 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "data": [
                                                {
                                                  "replyId": 1001,
                                                  "commentId": 456,
                                                  "userNickname": "주식고수",
                                                  "content": "저도 그렇게 생각합니다.",
                                                  "createdAt": "2025-05-21T14:00:00",
                                                  "isAuthor": true,
                                                  "likeCount": 4,
                                                  "isLiked": true
                                                },
                                                {
                                                  "replyId": 1002,
                                                  "commentId": 456,
                                                  "userNickname": "개미투자자",
                                                  "content": "좋은 정보 감사합니다!",
                                                  "createdAt": "2025-05-21T14:10:00",
                                                  "isAuthor": false,
                                                  "likeCount": 2,
                                                  "isLiked": false
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해당 댓글이 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
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
    public ResponseEntity<ReplyListResponseDto> getReplyList(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        ReplyListResponseDto result = replyService.getReplies(commentId, userId);

        return ResponseEntity.ok(result);
    }
}
