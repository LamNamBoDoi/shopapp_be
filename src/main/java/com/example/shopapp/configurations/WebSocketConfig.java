package com.example.shopapp.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Cho Flutter app (raw WebSocket)
        registry.addEndpoint("/ws")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOriginPatterns("*");

        // 2. Cho web frontend (dùng SockJS fallback)
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOrigins("http://localhost:3000")
                .addInterceptors(authHandshakeInterceptor)
                .withSockJS(); // SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");// bật broker trong ram để gửi tin
        registry.setApplicationDestinationPrefixes("/app");// các message client bắt đầu bằng /app/** sẽ định tuyến vào controller
        registry.setUserDestinationPrefix("/user");// gửi tin nhẵn riêng
    }
}
