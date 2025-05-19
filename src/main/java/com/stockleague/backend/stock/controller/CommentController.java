package com.stockleague.backend.stock.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.stock.dto.request.CommentCreateRequestDto;
import com.stockleague.backend.stock.dto.response.CommentCreateResponseDto;
import com.stockleague.backend.stock.dto.response.CommentLikeResponseDto;
import com.stockleague.backend.stock.service.CommentService;
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
@RequestMapping("/api/v1/stocks")
@Tag(name = "Comment", description = "주식 커뮤니티 관련 API")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{ticker}/comments")
    @Operation(summary = "댓글 작성", description = "특정 주식 종목에 댓글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "댓글 작성 성공",
                    content = @Content(schema = @Schema(implementation = CommentCreateRequestDto.class),
                            examples = @ExampleObject(
                                    name = "CommentCreateSuccess",
                                    summary = "댓글 등록 완료",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "댓글이 등록되었습니다.",
                                              "commentId": 456,
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
    public ResponseEntity<CommentCreateResponseDto> createComment(
            @Valid @RequestBody CommentCreateRequestDto request,
            @PathVariable String ticker,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        CommentCreateResponseDto result = commentService.createComment(request, ticker, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{commentId}/like")
    @Operation(summary = "댓글 좋아요 토글", description = "특정 댓글에 대해 좋아요를 누르거나 취소합니다. 유튜브 스타일 토글 방식입니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "좋아요 토글 성공",
                    content = @Content(
                            schema = @Schema(implementation = CommentLikeResponseDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "LikeSuccess",
                                            summary = "좋아요 성공",
                                            value = """
                                                        {
                                                          "success": true,
                                                          "message": "좋아요를 등록되었습니다.",
                                                          "isLiked" : true,
                                                          "likeCount": 17
                                                        }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "UnlikeSuccess",
                                            summary = "좋아요 취소 성공",
                                            value = """
                                                        {
                                                          "success": true,
                                                          "message": "좋아요가 취소되었습니다.",
                                                          "isLiked" : false,
                                                          "likeCount": 16
                                                        }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "404", description = "댓글이 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CommentNotFound",
                                    summary = "해당 티커에 대한 주식 정보가 존재하지 않을 경우",
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
    public ResponseEntity<CommentLikeResponseDto> toggleLike(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        CommentLikeResponseDto result = commentService.toggleCommentLike(commentId, userId);

        return ResponseEntity.ok(result);
    }
}
