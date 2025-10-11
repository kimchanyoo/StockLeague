package com.stockleague.backend.global.handler;

import com.stockleague.backend.global.security.StompPrincipal;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Component
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        Object uid = attributes.get("ws.userId");
        if (uid != null) {
            return new StompPrincipal(uid.toString());
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}
