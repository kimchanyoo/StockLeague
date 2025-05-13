package com.stockleague.backend.openapi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RealtimeKeyRequestDto(
        @JsonProperty("grant_type")
        String grantType,

        @JsonProperty("appkey")
        String appKey,

        @JsonProperty("appsecret")
        String appSecret
) {
}
