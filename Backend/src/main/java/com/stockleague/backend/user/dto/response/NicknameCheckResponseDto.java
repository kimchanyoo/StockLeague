package com.stockleague.backend.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NicknameCheckResponseDto {
    private boolean success;
    private boolean available;
    private String message;
}
