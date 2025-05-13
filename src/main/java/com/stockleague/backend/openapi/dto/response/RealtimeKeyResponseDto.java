package com.stockleague.backend.openapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RealtimeKeyResponseDto(
        @JsonProperty("approval_key")
        String approvalKey,

        @JsonProperty("expires_in")
        int expiresIn,

        @JsonProperty("scope")
        String scope
) {
}
