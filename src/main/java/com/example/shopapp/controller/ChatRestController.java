package com.example.shopapp.controller;

import com.example.shopapp.dtos.ChatRoomDTO;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.response.ChatMessageResponse;
import com.example.shopapp.response.ChatRoomResponse;
import com.example.shopapp.services.ChatMessage.IChatMessageService;
import com.example.shopapp.services.ChatRoom.IChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/chats")
@RequiredArgsConstructor
public class ChatRestController {
    private final IChatMessageService chatMessageService;
    private final IChatRoomService chatRoomService;
    @GetMapping("/history/{chatRoomId}")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getHistory(@PathVariable Long chatRoomId){
        return ResponseEntity.ok(ApiResponse.<List<ChatMessageResponse>>builder()
                .success(true)
                .message("200")
                .payload(chatMessageService.getMessagesByRoom(chatRoomId))
                .build());
    }

    @GetMapping("/history/{chatRoomId}/paginated")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getHistoryPaginated(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.<Page<ChatMessageResponse>>builder()
                .success(true)
                .message("200")
                .payload(chatMessageService.getMessagesByRoomPaginated(chatRoomId, pageable))
                .build());
    }

    @PostMapping("/room")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetRoom(@RequestBody ChatRoomDTO chatRoomDTO) {
        ChatRoomResponse room = chatRoomService.findOrCreateRoom(chatRoomDTO);
        return ResponseEntity.ok(ApiResponse.<ChatRoomResponse>builder()
                .success(true)
                .message("200")
                .payload(room)
                .build());
    }

    @GetMapping("/room/admin/{adminId}")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getAdminRooms(@PathVariable Long adminId) {
        return ResponseEntity.ok(ApiResponse.<List<ChatRoomResponse>>builder()
                .success(true)
                .message("200")
                .payload(chatRoomService.getActiveRoomsByAdmin(adminId))
                .build());
    }

    @GetMapping("/room/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getCustomerRooms(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.<List<ChatRoomResponse>>builder()
                .success(true)
                .message("200")
                .payload(chatRoomService.getActiveRoomsByCustomer(customerId))
                .build());
    }

    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long messageId) {
        chatMessageService.markAsRead(messageId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("200")
                .build());
    }


    @GetMapping("/messages/unread/{userId}")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .message("200")
                .payload(chatMessageService.getUnreadCount(userId))
                .build());
    }

    @PutMapping("/messages/room/{roomId}/read/{userId}")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(@PathVariable Long roomId, @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.<Integer>builder()
                .success(true)
                .message("200")
                .payload(chatMessageService.markAllAsRead(roomId, userId))
                .build());
    }

    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<Void>> closeRoom(@PathVariable Long roomId) {
        chatRoomService.markAsInactive(roomId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("200")
                .build());
    }
}
