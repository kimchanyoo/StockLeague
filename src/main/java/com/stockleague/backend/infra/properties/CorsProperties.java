package com.stockleague.backend.infra.properties;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@ConfigurationProperties(prefix = "cors")
@Configuration
public class CorsProperties {

    private List<String> allowedOrigins;

    public void setAllowedOrigins(String origins) {
        // .env에서 "A,B,C" 형태로 받으면 List<String>으로 변환
        this.allowedOrigins = Arrays.asList(origins.split(","));
    }

}
