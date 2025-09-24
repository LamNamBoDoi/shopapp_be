package com.example.shopapp.controller;

import com.example.shopapp.dtos.NotificationDTO;
import com.example.shopapp.enums.DeviceType;
import com.example.shopapp.response.NotificationResponse;
import com.example.shopapp.services.Firebase.IFirebaseMessaginService;
import com.example.shopapp.services.Notify.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/register-device")
    public ResponseEntity<Void> registerDevice(
            @RequestParam Long userId,
            @RequestParam String deviceToken,
            @RequestParam DeviceType deviceType,
            @RequestParam(required = false) String deviceName) {

        notificationService.registerDevice(userId, deviceToken, deviceType, deviceName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<NotificationResponse> notifications =
                notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        Long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {

        boolean success = notificationService.markAsRead(notificationId, userId);
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // Test endpoint - chỉ để development
    @PostMapping("/send-test")
    public ResponseEntity<Void> sendTestNotification(@RequestBody NotificationDTO dto) {
        notificationService.sendNotification(dto);
        return ResponseEntity.ok().build();
    }
}
