package com.stockleague.backend.admin.controller;

import com.stockleague.backend.admin.dto.request.AdminUserForceWithdrawRequestDto;
import com.stockleague.backend.admin.dto.response.AdminUserForceWithdrawResponseDto;
import com.stockleague.backend.admin.service.AdminService;
import com.stockleague.backend.global.exception.ErrorResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "관리자 관련 API")
public class AdminController {

    private final AdminService adminService;

    @PatchMapping("/users/{userId}")
    @Operation(summary = "회원 이용 정지", description = "관리자가 특정 회원을 이용 정지시킵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 이용 정지 성공",
                    content = @Content(schema = @Schema(implementation = AdminUserForceWithdrawResponseDto.class),
                            examples = @ExampleObject(
                                    name = "UserBanSuccess",
                                    summary = "회원 이용 정지 완료",
                                    value = """
                                                {
                                                  "success": true,
                                                  "message": "회원이 이용 정지되었습니다."
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
                                                  "errorCode": "FORBIDDEN"
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
    public ResponseEntity<AdminUserForceWithdrawResponseDto> forceWithdrawUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserForceWithdrawRequestDto request,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();

        AdminUserForceWithdrawResponseDto response = adminService.forceWithdrawUser(request, adminId, userId);
        return ResponseEntity.ok(response);
    }
}
