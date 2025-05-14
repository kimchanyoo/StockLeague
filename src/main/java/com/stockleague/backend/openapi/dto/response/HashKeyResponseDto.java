package com.stockleague.backend.openapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HashKeyResponseDto(
        @JsonProperty("HASH")
        String hash
) {
}
