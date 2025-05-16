package com.stockleague.backend.user.controller;

import com.stockleague.backend.auth.service.AuthService;
import com.stockleague.backend.user.dto.request.UserProfileUpdateRequestDto;
import com.stockleague.backend.user.dto.request.UserWithdrawRequestDto;
import com.stockleague.backend.user.dto.response.UserProfileResponseDto;
import com.stockleague.backend.user.dto.response.UserProfileUpdateResponseDto;
import com.stockleague.backend.user.dto.response.UserWithdrawResponseDto;
import com.stockleague.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/profile")
    @Operation(summary = "회원 정보 읽기", description = "사용자가 자신의 회원 정보를 읽을 수 있음")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 불러오기 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class),
                            examples = @ExampleObject(
                                    name = "UserProfile",
                                    summary = "회원 정보 읽기",
                                    value = """
                                            {
                                                "success" : true,
                                                "message" : "회원 정보를 가져오는데 성공했습니다.",
                                                "nickname" : "소셜유저",
                                                "role" : "ADMIN"
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
            )
    })
    public ResponseEntity<UserProfileResponseDto> getUserProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserProfileResponseDto user = userService.getUserProfile(userId);

        return ResponseEntity.ok(user);
    }

    @PatchMapping("/profile")
    @Operation(summary = "회원 정보 수정", description = "사용자가 자신의 회원 정보를 수정할 수 있음")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class),
                            examples = @ExampleObject(
                                    name = "UserProfileUpdate",
                                    summary = "회원 정보 수정",
                                    value = """
                                            {
                                                "success" : true,
                                                "message" : "회원 정보가 수정되었습니다.",
                                                "nickname" : "김찬유"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "닉네임 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "InvalidNicknameFormat",
                                            summary = "올바르지 않은 닉네임 형식",
                                            value = """
                                                    {
                                                       "success" : false,
                                                       "message" : "닉네임 형식이 올바르지 않습니다.",
                                                       "errorCode": "NICKNAME_FORMAT_INVALID"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "DuplicatedNickname",
                                            summary = "이미 사용 중인 닉네임",
                                            value = """
                                                    {
                                                       "success" : false,
                                                       "message" : "이미 사용 중인 닉네임입니다.",
                                                       "errorCode": "DUPLICATED_NICKNAME"
                                                    }
                                                    """
                                    )
                            }
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
            )
    })
    public ResponseEntity<UserProfileUpdateResponseDto> updateUserProfile(
            Authentication authentication,
            @RequestBody @Valid UserProfileUpdateRequestDto request) {
        Long userId = (Long) authentication.getPrincipal();
        UserProfileUpdateResponseDto user = userService.updateUserProfile(userId, request);

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/withdraw")
    @Operation(
            summary = "회원 탈퇴",
            description = "사용자가 '탈퇴합니다.' 문구를 입력하여 회원 탈퇴를 요청합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserWithdrawResponseDto.class),
                            examples = @ExampleObject(
                                    name = "UserWithdrawSuccess",
                                    summary = "회원 탈퇴 완료",
                                    value = """
                                            {
                                                "success": true,
                                                "message": "회원 탈퇴가 완료되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "탈퇴 문구가 일치하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidWithdrawConfirmMessage",
                                    summary = "탈퇴 문구 불일치",
                                    value = """
                                            {
                                                "success": false,
                                                "message": "탈퇴 문구가 일치하지 않습니다.",
                                                "errorCode": "INVALID_WITHDRAW_CONFIRM_MESSAGE"
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
            )
    })
    public ResponseEntity<UserWithdrawResponseDto> withdrawUser(
            Authentication authentication,
            HttpServletRequest servletRequest,
            HttpServletResponse response,
            @RequestBody @Valid UserWithdrawRequestDto request) {
        Long userId = (Long) authentication.getPrincipal();

        UserWithdrawResponseDto result = userService.deleteUser(userId, request);
        authService.clearUserTokens(userId, servletRequest, response);

        return ResponseEntity.ok(result);
    }
}
