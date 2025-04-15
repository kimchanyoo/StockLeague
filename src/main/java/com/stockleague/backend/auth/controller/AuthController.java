package com.stockleague.backend.auth.controller;

import com.stockleague.backend.auth.dto.request.AdditionalInfoRequestDto;
import com.stockleague.backend.auth.dto.request.OAuthLoginRequestDto;
import com.stockleague.backend.auth.dto.request.RefreshTokenRequestDto;
import com.stockleague.backend.auth.dto.response.OAuthLoginResponseDto;
import com.stockleague.backend.auth.dto.response.TokenReissueResponseDto;
import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.auth.service.AuthService;
import com.stockleague.backend.auth.service.OAuthLoginService;
import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.TokenRedisService;
import com.stockleague.backend.user.dto.response.NicknameCheckResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name="Auth", description = "인증 관련 API")
public class AuthController {

    private final JwtProvider jwtProvider;
    private final AuthService authService;
    private final TokenRedisService redisService;
    private final OAuthLoginService oAuthLoginService;

    @PostMapping("/oauth/login")
    @Operation(summary = "소셜 로그인", description = "클라이언트로부터 받은 소셜 로그인 인가 코드를 바탕으로 사용자 인증 및 JWT 토큰 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "소셜 로그인 성공",
                    content = @Content(schema = @Schema(implementation = OAuthLoginResponseDto.class),
                        examples = {
                            @ExampleObject(
                                name = "SocialLoginSuccess",
                                summary = "소셜 로그인 성공",
                                value = """
                                    {
                                       "success" : true,
                                       "message" : "소셜 로그인 성공",
                                       "accessToken" : "eyJhbGciOiJIUzI1...",
                                       "refreshToken": "c82h3g2h3...",
                                       "isFirstLogin" : false
                                    }
                                    """
                            ),
                            @ExampleObject(
                                    name = "FirstLoginRequiresAdditionalInfo",
                                    summary = "최초 로그인: 추가 정보 필요",
                                    value = """
                                    {
                                       "success" : true,
                                       "message" : "소셜 로그인 성공",
                                       "accessToken" : "eyJhbGciOiJIUzI1...",
                                       "refreshToken": null,
                                       "isFirstLogin" : true
                                    }
                                    """
                            )
                        }
                    )
            ),
            @ApiResponse(responseCode = "400", description = "소셜 로그인 인증에 실패했습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "OAuthAuthFailed",
                                    summary = "로그인 인증 실패",
                                    value = """
                                    {
                                       "success" : false,
                                       "message" : "소셜 로그인 인증에 실패했습니다.",
                                       "errorCode": "OAUTH_AUTH_FAILED"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "소셜 로그인 서버와 통신 중 문제가 발생했습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "OAuthAuthFailed",
                                    summary = "서버 통신 오류",
                                    value = """
                                    {
                                       "success" : false,
                                       "message" : "소셜 로그인 서버와 통신 중 문제가 발생했습니다.",
                                       "errorCode": "OAUTH_SERVER_ERROR"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<OAuthLoginResponseDto> socialLogin(@RequestBody @Valid OAuthLoginRequestDto request) {
        return ResponseEntity.ok(oAuthLoginService.login(request));
    }

    @PostMapping("/oauth/complete")
    @Operation(summary = "소셜 로그인 추가 정보 입력", description = "최초 로그인 시 추가 정보를 받아 회원가입을 완료한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 완료",
                    content = @Content(schema = @Schema(implementation = OAuthLoginResponseDto.class),
                            examples = @ExampleObject(
                                name = "CompleteLogin",
                                summary = "추가 정보 입력 완료",
                                value = """
                                    {
                                       "success" : true,
                                       "message" : "추가 정보 입력이 완료되었습니다",
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "이미 가입된 유저",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AlreadyRegisteredUser",
                                    summary = "가입된 유저",
                                    value = """
                                    {
                                       "success" : false,
                                       "message" : "이미 가입된 유저입니다.",
                                       "errorCode": "ALREADY_REGISTERED"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 임시 토큰",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidTempToken",
                                    summary = "유효하지 않은 임시 토큰",
                                    value = """
                                    {
                                       "success" : false,
                                       "message" : "임시 토큰이 유효하지 않습니다.",
                                       "errorCode": "ALREADY_REGISTERED"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<OAuthLoginResponseDto> completeSignup(
            HttpServletRequest request,
            @Valid @RequestBody AdditionalInfoRequestDto requestDto
    ) {
        String token = jwtProvider.resolveToken(request);
        OAuthLoginResponseDto response = authService.completeSignup(token, requestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 통해 Access Token을 재발급 받는다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재발급 성공",
                    content = @Content(schema = @Schema(implementation = TokenReissueResponseDto.class),
                            examples = @ExampleObject(
                                    name = "ReissuanceAccessToken",
                                    summary = "엑세스 토큰 재발급",
                                    value = """
                                    {
                                       "success" : true,
                                       "accessToken" : "eyJhbGciOiJIUzI1...",
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "리프레시 토큰이 유효하지 않음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidRefreshToken",
                                    summary = "리프레시 토큰이 유효하지 않음",
                                    value = """
                                    {
                                       "success" : false,
                                       "message" : "리프레시 토큰이 유효하지 않습니다.",
                                       "errorCode": "INVALID_REFRESH_TOKEN"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰이 만료되었습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ExpiredRefreshToken",
                                    summary = "리프레시 토큰 만료",
                                    value = """
                                    {
                                       "success" : false,
                                       "message" : "리프레시 토큰이 만료되었습니다.",
                                       "errorCode": "EXPIRED_REFRESH_TOKEN"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<?> refreshAccessToken(@RequestBody @Valid RefreshTokenRequestDto requestDto){
        String refreshToken = requestDto.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new GlobalException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        String savedToken = redisService.getRefreshToken(userId);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new GlobalException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtProvider.createAccessToken(userId);

        return ResponseEntity.ok(
                TokenReissueResponseDto.builder()
                        .success(true)
                        .accessToken(newAccessToken)
                        .build()
        );
    }

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 검사", description = "사용자가 입력한 닉네임이 중복되는지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 검사 성공",
                    content = @Content(schema = @Schema(implementation = NicknameCheckResponseDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "사용 가능",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "available": true,
                                                      "message": "사용 가능한 닉네임입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "사용 중",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "available": false,
                                                      "message": "이미 사용 중인 닉네임입니다."
                                                    }
                                                    """
                                    )
                            })),
            @ApiResponse(responseCode = "400", description = "닉네임 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
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
            )
    })
    public ResponseEntity<NicknameCheckResponseDto> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(authService.checkNickname(nickname));
    }
}
