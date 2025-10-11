package com.stockleague.backend.notification.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.notification.domain.TargetType;
import com.stockleague.backend.notification.dto.NotificationListResponseDto;
import com.stockleague.backend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    @Operation(
            summary = "알림 목록 조회",
            description = "로그인한 사용자의 알림을 페이지 단위로 조회합니다. " +
                    "`status`로 읽음 상태를 필터링할 수 있습니다. (unread | read | all)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationListResponseDto.class),
                            examples = @ExampleObject(
                                    name = "NotificationListSuccess",
                                    summary = "알림 목록 조회 성공 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "content": [
                                                {
                                                  "notificationId": 101,
                                                  "type": "REPLY",
                                                  "message": "내 댓글에 대댓글이 달렸습니다.",
                                                  "target": "COMMENT",
                                                  "targetId": 555,
                                                  "isRead": false,
                                                  "createdAt": "2025-08-21T16:55:10"
                                                }
                                              ],
                                              "page": 1,
                                              "size": 10,
                                              "totalElements": 1,
                                              "totalPages": 1
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터(status 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidParam",
                                    summary = "유효하지 않은 status",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "잘못된 요청 파라미터입니다.",
                                              "errorCode": "INVALID_PARAM"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<NotificationListResponseDto> getList(
            Authentication authentication,
            @RequestParam(defaultValue = "unread") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = (Long) authentication.getPrincipal();

        NotificationListResponseDto result = service.getNotifications(userId, status, page, size);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    @Operation(
            summary = "읽지 않은 알림 개수 조회",
            description = "배지(badge) 표시용으로 읽지 않은 알림 수를 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "UnreadCountSuccess",
                            summary = "읽지 않은 개수 예시",
                            value = """
                                    {
                                      "success": true,
                                      "unreadCount": 7
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<Map<String, Object>> unreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        long count = service.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("success", true, "unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    @Operation(
            summary = "알림 단건 읽음 처리",
            description = "특정 알림을 읽음 상태로 변경합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "MarkReadSuccess",
                                    summary = "읽음 처리 성공 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "notificationId": 101,
                                              "isRead": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "NotificationNotFound",
                                    summary = "존재하지 않는 알림",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "해당 항목을 찾을 수 없습니다.",
                                              "errorCode": "NOTIFICATION_NOT_FOUND"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> markRead(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = (Long) authentication.getPrincipal();
        service.markRead(userId, id);
        return ResponseEntity.ok(Map.of("success", true, "notificationId", id, "isRead", true));
    }

    @PatchMapping("/read-all")
    @Operation(
            summary = "알림 전체 읽음 처리",
            description = "사용자의 모든 알림을 읽음 처리합니다. `target`을 지정하면 해당 대상만 처리합니다."
    )
    @ApiResponse(responseCode = "200", description = "일괄 읽음 처리 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "MarkAllReadSuccess",
                            summary = "일괄 읽음 처리 성공 예시",
                            value = """
                                    {
                                      "success": true,
                                      "updatedCount": 21
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<Map<String, Object>> markAllRead(
            Authentication authentication,
            @RequestParam(required = false) TargetType target
    ) {
        Long userId = (Long) authentication.getPrincipal();
        int updated = service.markAllRead(userId, target);
        return ResponseEntity.ok(Map.of("success", true, "updatedCount", updated));
    }

    @PatchMapping("/{id}/close")
    @Operation(
            summary = "알림 닫기(Soft Delete)",
            description = "알림을 닫아 목록에서 숨깁니다. 실제 삭제는 보관 기간 만료 후 일괄 삭제됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닫기 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "CloseSuccess",
                                    summary = "닫기 성공 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "notificationId": 101,
                                              "isClosed": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "다른 사용자 소유",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    summary = "권한 없음",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "접근 권한이 없습니다.",
                                              "errorCode": "FORBIDDEN"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "NotificationNotFound",
                                    summary = "존재하지 않는 알림",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "해당 항목을 찾을 수 없습니다.",
                                              "errorCode": "NOTIFICATION_NOT_FOUND"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> close(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = (Long) authentication.getPrincipal();
        service.markAsClose(userId, id);
        return ResponseEntity.ok(Map.of("success", true, "notificationId", id, "isClosed", true));
    }
}
