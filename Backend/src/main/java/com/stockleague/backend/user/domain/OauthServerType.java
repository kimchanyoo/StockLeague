package com.stockleague.backend.user.domain;

public enum OauthServerType {
    NAVER,
    KAKAO;

    public static OauthServerType fromName(String type){
        return OauthServerType.valueOf(type.toUpperCase());
    }
}
