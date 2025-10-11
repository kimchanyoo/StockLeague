package com.stockleague.backend.global.interceptor;

import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.infra.redis.TokenRedisService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtProvider jwtProvider;
    private final TokenRedisService redisService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractBearer(request);
        if (token == null) token = extractTokenFromQuery(request);
        if (token == null) token = extractTokenFromCookie(request);

        if (token != null) {
            try {
                if (jwtProvider.validateToken(token) && !redisService.isBlacklisted(token)) {
                    String userId = String.valueOf(jwtProvider.getUserId(token));
                    attributes.put("ws.userId", userId);
                } else {
                    log.debug("[WS] Handshake token invalid or blacklisted");
                }
            } catch (Exception e) {
                log.debug("[WS] Handshake token ignored: {}", e.getMessage());
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
    }

    private String extractBearer(ServerHttpRequest request) {
        List<String> auths = request.getHeaders().get("Authorization");
        String raw = (auths != null && !auths.isEmpty()) ? auths.get(0) : null;
        if (raw != null && raw.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return raw.substring(7).trim();
        }
        return null;
    }

    private String extractTokenFromQuery(ServerHttpRequest request) {
        String q = request.getURI().getQuery();
        if (q == null) return null;
        return Arrays.stream(q.split("&"))
                .filter(s -> s.startsWith("access_token="))
                .map(s -> s.substring("access_token=".length()))
                .findFirst().orElse(null);
    }

    private String extractTokenFromCookie(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest sr) {
            HttpServletRequest req = sr.getServletRequest();
            if (req.getCookies() != null) {
                var c = Arrays.stream(req.getCookies())
                        .filter(c0 -> "access_token".equals(c0.getName()))
                        .findFirst().orElse(null);
                if (c != null) return c.getValue();
            }
        }
        return null;
    }
}
