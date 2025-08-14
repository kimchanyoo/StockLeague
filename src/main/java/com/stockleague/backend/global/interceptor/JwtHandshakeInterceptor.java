package com.stockleague.backend.global.interceptor;

import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.infra.redis.TokenRedisService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtProvider jwtProvider;
    private final TokenRedisService redisService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);
        if (token != null && jwtProvider.validateToken(token) && !redisService.isBlacklisted(token)) {
            String userId = String.valueOf(jwtProvider.getUserId(token));
            attributes.put("ws.userId", userId);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) { }

    private String extractToken(ServerHttpRequest request) {
        List<String> q = request.getURI().getQuery() == null ? null :
                Arrays.stream(request.getURI().getQuery().split("&"))
                        .filter(s -> s.startsWith("access_token="))
                        .map(s -> s.substring("access_token=".length()))
                        .toList();
        if (q != null && !q.isEmpty()) return q.get(0);

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
