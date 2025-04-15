package com.stockleague.backend.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 요청 헤더에서 jwt추출
        String token = jwtProvider.resolveToken(request);

        // 토큰 유효성 검사
        if(token != null && jwtProvider.validateToken(token)) {

            String type = jwtProvider.parseClaims(token).get("type", String.class);

            if ("access".equals(type)) {
                //토큰에서 사용자 정보 꺼내기
                Authentication authentication = jwtProvider.getAuthentication(token);

                // SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }
}
