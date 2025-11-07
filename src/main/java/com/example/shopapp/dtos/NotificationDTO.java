package com.example.shopapp.dtos;

import com.example.shopapp.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long userId;
    private String title;
    private String body;
    private NotificationType type;
    private Map<String, Object> data;
}
