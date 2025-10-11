package com.stockleague.backend.auth.oauth.info;

import com.stockleague.backend.user.domain.OauthServerType;
import java.util.Map;

public class KaKaoUserInfo implements OAuthUserInfo {

    private Map<String, Object> attributes;

    public KaKaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getOauthId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getNickname() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");
        return (String) profile.get("nickname");
    }

    @Override
    public OauthServerType getProvider() {
        return OauthServerType.KAKAO;
    }
}
