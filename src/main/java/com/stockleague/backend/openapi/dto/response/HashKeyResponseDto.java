package com.stockleague.backend.openapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HashKeyResponseDto(

        @JsonProperty("JsonBody")
        Object jsonBody,

        @JsonProperty("HASH")
        String hash
) {
}
