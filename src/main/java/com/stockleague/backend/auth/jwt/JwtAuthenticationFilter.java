package com.stockleague.backend.auth.jwt;

import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.TokenRedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenRedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        final String path = request.getRequestURI();
        final boolean isRefreshPath = path.startsWith("/api/v1/auth/token/refresh");
        final boolean isLogoutPath  = path.startsWith("/api/v1/auth/logout");

        String token = resolveToken(request);
        if (token != null) {
            log.info("[JWT] 추출된 accessToken (앞 10자): {}", token.substring(0, Math.min(10, token.length())));
        }

        if (token != null && jwtProvider.validateToken(token)) {

            if (redisService.isBlacklisted(token)) {
                log.warn("블랙리스트에 등록된 토큰입니다. path={}", path);
                if (isRefreshPath || isLogoutPath) {
                    chain.doFilter(request, response);
                    return;
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (isRefreshPath || isLogoutPath) {
                chain.doFilter(request, response);
                return;
            }

            try {
                String type = jwtProvider.parseClaims(token).get("type", String.class);
                if ("access".equals(type)) {
                    Authentication authentication = jwtProvider.getAuthentication(token); // 여기서 DB 조회
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (GlobalException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        chain.doFilter(request, response);
    }


    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }
}
