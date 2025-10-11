package com.stockleague.backend.infra.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openapi")
@Getter
@Setter
public class OpenApiProperties {
    private String authUrl;
    private String appKey;
    private String appSecret;
    private String grantType;
    private String scope;
}
