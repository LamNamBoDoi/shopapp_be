package com.example.shopapp.controller;

import com.example.shopapp.dtos.NotificationDTO;
import com.example.shopapp.enums.DeviceType;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.response.NotificationResponse;
import com.example.shopapp.services.Firebase.IFirebaseMessaginService;
import com.example.shopapp.services.Notify.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @PostMapping("/register-device")
    public ResponseEntity<ApiResponse<Void>> registerDevice(
            @RequestParam Long userId,
            @RequestParam String deviceToken,
            @RequestParam DeviceType deviceType,
            @RequestParam(required = false) String deviceName) {

        notificationService.registerDevice(userId, deviceToken, deviceType, deviceName);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Register device success")
                .build());
    }

    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<NotificationResponse> notifications =
                notificationService.getUserNotifications(userId, page, size);

        return ResponseEntity.ok(ApiResponse.<Page<NotificationResponse>>builder()
                .success(true)
                .message("Fetched notifications successfully")
                .payload(notifications)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
        Long count = notificationService.countUnread(userId);

        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .message("Unread count fetched")
                .payload(count)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {

        boolean success = notificationService.markAsRead(notificationId, userId);

        if (success) {
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Marked as read successfully")
                    .build());
        } else {
            return ResponseEntity.status(404).body(ApiResponse.<Void>builder()
                    .success(false)
                    .message("Notification not found")
                    .build());
        }
    }

    // Test endpoint - chỉ để development
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/send-test")
    public ResponseEntity<ApiResponse<Void>> sendTestNotification(@RequestBody NotificationDTO dto) {
        notificationService.sendNotification(dto);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Test notification sent")
                .build());
    }
}
