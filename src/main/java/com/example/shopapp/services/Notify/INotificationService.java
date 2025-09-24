package com.example.shopapp.services.Notify;

import com.example.shopapp.dtos.NotificationDTO;
import com.example.shopapp.enums.DeviceType;
import com.example.shopapp.response.NotificationResponse;
import org.springframework.data.domain.Page;

public interface INotificationService {
    void sendNotification(NotificationDTO dto);
    Page<NotificationResponse> getUserNotifications(Long userId, int page, int size);
    Long countUnread(Long userId);
    boolean markAsRead(Long notificationId, Long userId);
    void registerDevice(Long userId, String deviceToken, DeviceType deviceType, String deviceName);
}
