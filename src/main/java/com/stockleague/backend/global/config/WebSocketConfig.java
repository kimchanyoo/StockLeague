package com.stockleague.backend.global.config;

import com.stockleague.backend.global.handler.JwtHandshakeHandler;
import com.stockleague.backend.global.interceptor.JwtHandshakeInterceptor;
import com.stockleague.backend.global.interceptor.WebSocketSecurityInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketSecurityInterceptor webSocketSecurityInterceptor;
    private final JwtHandshakeHandler jwtHandshakeHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Bean
    public ThreadPoolTaskScheduler wsTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(jwtHandshakeHandler)
                .setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws-sockjs")
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(jwtHandshakeHandler)
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setSessionCookieNeeded(false);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/user/queue")
                .setTaskScheduler(wsTaskScheduler())
                .setHeartbeatValue(new long[]{10_000, 10_000});
        registry.setUserDestinationPrefix("/user");
        registry.setPreservePublishOrder(true);
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration
                .interceptors(webSocketSecurityInterceptor)
                .taskExecutor()
                .corePoolSize(8)
                .maxPoolSize(32)
                .queueCapacity(1000)
                .keepAliveSeconds(60);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration
                .taskExecutor()
                .corePoolSize(8)
                .maxPoolSize(32)
                .queueCapacity(1000)
                .keepAliveSeconds(60);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry
                .setMessageSizeLimit(512 * 1024)
                .setSendBufferSizeLimit(3 * 1024 * 1024)
                .setSendTimeLimit(20_000);
    }
}
