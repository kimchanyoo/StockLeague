package com.stockleague.backend.user.controller;

import com.stockleague.backend.user.dto.response.UserAssetResponseDto;
import com.stockleague.backend.user.dto.response.UserProfileResponseDto;
import com.stockleague.backend.user.service.UserAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/asset")
@Tag(name = "Asset", description = "유저 자산 관련 API")
public class UserAssetController {

    private final UserAssetService userAssetService;

    @GetMapping
    @Operation(summary = "회원 자산 정보 읽기", description = "사용자가 자신의 자산 정보를 읽을 수 있음")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 자산 정보 불러오기 성공",
                    content = @Content(schema = @Schema(implementation = UserAssetResponseDto.class),
                            examples = @ExampleObject(
                                    name = "UserAsset",
                                    summary = "회원 자산 정보 읽기",
                                    value = """
                                            {
                                                "success" : true,
                                                "message" : "회원 자산 정보를 가져오는데 성공했습니다.",
                                                "userId" : 456,
                                                "cashBalance" : "100000000.00",
                                                "totalValuation" : "1500000000.00",
                                                "updatedAt" : "2025-07-08T14:30:30"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해당 사용자가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "UserNotFound",
                                            summary = "존재하지 않는 사용자",
                                            value = """
                                                    {
                                                    "success" : false,
                                                    "message" : "해당 사용자를 찾을 수 없습니다.",
                                                    "errorCode": "USER_NOT_FOUND"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "AssetNotFound",
                                            summary = "자산이 존재하지 않음",
                                            value = """
                                                    {
                                                    "success" : false,
                                                    "message" : "해당 유저의 자산 정보가 존재하지 않습니다.",
                                                    "errorCode": "ASSET_NOT_FOUND"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<UserAssetResponseDto> getUserProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserAssetResponseDto user = userAssetService.getUserAsset(userId);

        return ResponseEntity.ok(user);
    }
}
