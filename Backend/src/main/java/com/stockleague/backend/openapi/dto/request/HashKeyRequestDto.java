package com.stockleague.backend.openapi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HashKeyRequestDto(
        @JsonProperty("data")
        Object data
) {
}
