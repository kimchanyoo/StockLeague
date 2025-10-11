package com.stockleague.backend.auth.oauth.client;

import com.stockleague.backend.auth.dto.request.OAuthLoginRequestDto;
import com.stockleague.backend.auth.oauth.info.OAuthUserInfo;
import com.stockleague.backend.user.domain.OauthServerType;

public interface OAuthClient {
    boolean supports(OauthServerType provider);
    OAuthUserInfo requestUserInfo(OAuthLoginRequestDto requestDto);
}
