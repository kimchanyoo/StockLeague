package com.stockleague.backend.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeCreateRequestDto(

        @NotBlank(message = "공지 제목은 필수입니다.")
        @Size(max = 200, message = "제목은 최대 200자까지 가능합니다.")
        String title,

        @NotBlank(message = "공지 유형은 필수입니다.")
        @Size(max = 50, message = "공지 유형은 최대 50자까지 가능합니다.")
        String category,

        @NotBlank(message = "공지 내용은 필수입니다.")
        String content
) {}
