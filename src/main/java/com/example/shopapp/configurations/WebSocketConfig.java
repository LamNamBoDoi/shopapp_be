package com.example.shopapp.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // endpoint cho Flutter/Web kết nối
                .setAllowedOriginPatterns("*") // cấu hình CORS
                .withSockJS(); // fallback nếu không hỗ trợ WebSocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");// bật broker trong ram để gửi tin
        registry.setApplicationDestinationPrefixes("/app");// các message client bắt đầu bằng /app/** sẽ định tuyến vào controller
        registry.setUserDestinationPrefix("/user");// gửi tin nhẵn riêng
    }
}
