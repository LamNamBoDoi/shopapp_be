package com.example.shopapp.response;

import com.example.shopapp.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String body;
    private NotificationType type;
    private Map<String, Object> data;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
