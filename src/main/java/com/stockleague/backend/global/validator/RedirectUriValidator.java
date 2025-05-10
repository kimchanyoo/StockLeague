package com.stockleague.backend.global.validator;

import java.util.Set;

public class RedirectUriValidator {

    private RedirectUriValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final Set<String> WHITELIST = Set.of(
            "http://localhost:3000/auth/callback",
            "http://130.162.145.59:8080/login/oauth2/code/kakao"
    );

    public static boolean isAllowed(String redirectUri) {
        return WHITELIST.contains(redirectUri);
    }
}
