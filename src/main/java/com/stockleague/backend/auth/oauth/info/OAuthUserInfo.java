package com.stockleague.backend.auth.oauth.info;

import com.stockleague.backend.user.domain.OauthServerType;

public interface OAuthUserInfo {
    String getOauthId();
    String getNickname();
    OauthServerType getProvider();
}
