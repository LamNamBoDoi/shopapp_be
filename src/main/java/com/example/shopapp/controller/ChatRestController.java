package com.example.shopapp.controller;

import com.example.shopapp.models.ChatMessage;
import com.example.shopapp.models.ChatRoom;
import com.example.shopapp.services.ChatMessage.IChatMessageService;
import com.example.shopapp.services.ChatRoom.IChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {
    private final IChatMessageService chatMessageService;
    private final IChatRoomService chatRoomService;
    @GetMapping("/history/{chatRoomId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable Long chatRoomId){
        return ResponseEntity.ok(chatMessageService.getMessagesByRoom(chatRoomId));
    }

    @GetMapping("/history/{chatRoomId}/paginated")
    public ResponseEntity<Page<ChatMessage>> getHistoryPaginated(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Pageable pageable = PageRequest.of(page,size);
        return ResponseEntity.ok(chatMessageService.getMessagesByRoomPaginated(chatRoomId, pageable));
    }

    @PostMapping("/room")
    public ResponseEntity<ChatRoom> createOrGetRoom(@RequestBody Map<String, Long> request) {
        Long customerId = request.get("customerId");
        Long adminId = request.get("adminId");
        ChatRoom room = chatRoomService.findOrCreateRoom(customerId, adminId);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/rooms/admin/{adminId}")
    public ResponseEntity<List<ChatRoom>> getAdminRooms(@PathVariable Long adminId) {
        return ResponseEntity.ok(chatRoomService.getActiveRoomsByAdmin(adminId));
    }

    @GetMapping("/rooms/customer/{customerId}")
    public ResponseEntity<List<ChatRoom>> getCustomerRooms(@PathVariable Long customerId) {
        return ResponseEntity.ok(chatRoomService.getActiveRoomsByCustomer(customerId));
    }

    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
        chatMessageService.markAsRead(messageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/unread/{userId}")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(chatMessageService.getUnreadCount(userId));
    }

    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<Void> closeRoom(@PathVariable Long roomId) {
        chatRoomService.markAsInactive(roomId);
        return ResponseEntity.ok().build();
    }
}
