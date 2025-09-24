package com.example.shopapp.services.Notify;

import com.example.shopapp.dtos.NotificationDTO;
import com.example.shopapp.enums.DeviceType;
import com.example.shopapp.models.Notification;
import com.example.shopapp.models.UserDevice;
import com.example.shopapp.repositories.NotificationRepository;
import com.example.shopapp.repositories.UserDeviceRepository;
import com.example.shopapp.response.NotificationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final ObjectMapper objectMapper;

    /**
     * Gửi thông báo (lưu DB + gửi FCM)
     */
    public void sendNotification(NotificationDTO dto) {
        try {
            // 1. Lưu vào database
            Notification notification = saveNotificationToDB(dto);

            // 2. Gửi FCM cho tất cả devices của user
            sendFCMToUser(dto.getUserId(), dto.getTitle(), dto.getBody(), dto.getData());

            log.info("Notification sent successfully to user: {}", dto.getUserId());

        } catch (Exception e) {
            log.error("Failed to send notification to user: {}", dto.getUserId(), e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    /**
     * Lưu notification vào DB
     */
    private Notification saveNotificationToDB(NotificationDTO dto) {
        try {
            String dataJson = dto.getData() != null ?
                    objectMapper.writeValueAsString(dto.getData()) : null;

            Notification notification = Notification.builder()
                    .userId(dto.getUserId())
                    .title(dto.getTitle())
                    .body(dto.getBody())
                    .type(dto.getType())
                    .data(dataJson)
                    .createdAt(LocalDateTime.now())
                    .build();

            return notificationRepository.save(notification);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save notification to DB", e);
        }
    }

    /**
     * Gửi FCM cho tất cả devices của user
     */
    private void sendFCMToUser(Long userId, String title, String body, Map<String, Object> data) {
        List<UserDevice> devices = userDeviceRepository.findByUserIdAndIsActiveTrue(userId);

        if (devices.isEmpty()) {
            log.warn("No active devices found for user: {}", userId);
            return;
        }

        // Chuẩn bị data cho FCM
        Map<String, String> fcmData = new HashMap<>();
        if (data != null) {
            data.forEach((key, value) -> fcmData.put(key, String.valueOf(value)));
        }

        // Gửi từng device (hoặc có thể gửi batch)
        for (UserDevice device : devices) {
            sendFCMToDevice(device.getDeviceToken(), title, body, fcmData);
        }
    }

    /**
     * Gửi FCM tới 1 device
     */
    private void sendFCMToDevice(String token, String title, String body, Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = firebaseMessaging.send(messageBuilder.build());
            log.debug("FCM sent successfully: {}", response);

        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                // Token không hợp lệ, deactivate
                userDeviceRepository.deactivateByToken(token);
                log.warn("Deactivated invalid FCM token: {}", token);
            } else {
                log.error("Failed to send FCM to token: {}", token, e);
            }
        } catch (Exception e) {
            log.error("Unexpected error sending FCM to token: {}", token, e);
        }
    }

    /**
     * Lấy danh sách thông báo của user (phân trang)
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return notifications.map(this::convertToResponse);
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    @Transactional(readOnly = true)
    public Long countUnread(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * Đánh dấu đã đọc
     */
    public boolean markAsRead(Long notificationId, Long userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId, LocalDateTime.now());
        return updated > 0;
    }

    /**
     * Đăng ký device token
     */
    public void registerDevice(Long userId, String deviceToken, DeviceType deviceType, String deviceName) {
        Optional<UserDevice> existing = userDeviceRepository.findByDeviceToken(deviceToken);

        if (existing.isPresent()) {
            // Update existing device
            UserDevice device = existing.get();
            device.setUserId(userId);
            device.setDeviceType(deviceType);
            device.setDeviceName(deviceName);
            device.setIsActive(true);
            userDeviceRepository.save(device);
        } else {
            // Create new device
            UserDevice newDevice = UserDevice.builder()
                    .userId(userId)
                    .deviceToken(deviceToken)
                    .deviceType(deviceType)
                    .deviceName(deviceName)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userDeviceRepository.save(newDevice);
        }

        log.info("Device registered for user: {} with token: {}", userId, deviceToken.substring(0, 20) + "...");
    }

    private NotificationResponse convertToResponse(Notification notification) {
        Map<String, Object> data = null;
        if (notification.getData() != null) {
            try {
                data = objectMapper.readValue(notification.getData(), Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse notification data", e);
            }
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getType())
                .data(data)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
