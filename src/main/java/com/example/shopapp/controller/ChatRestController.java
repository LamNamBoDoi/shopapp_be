package com.example.shopapp.controller;

import com.example.shopapp.dtos.ChatRoomDTO;
import com.example.shopapp.models.ChatMessage;
import com.example.shopapp.models.ChatRoom;
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
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/chats")
@RequiredArgsConstructor
public class ChatRestController {
    private final IChatMessageService chatMessageService;
    private final IChatRoomService chatRoomService;
    @GetMapping("/history/{chatRoomId}")
    public ResponseEntity<List<ChatMessageResponse>> getHistory(@PathVariable Long chatRoomId){
        return ResponseEntity.ok(chatMessageService.getMessagesByRoom(chatRoomId));
    }

    @GetMapping("/history/{chatRoomId}/paginated")
    public ResponseEntity<Page<ChatMessageResponse>> getHistoryPaginated(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Pageable pageable = PageRequest.of(page,size);
        return ResponseEntity.ok(chatMessageService.getMessagesByRoomPaginated(chatRoomId, pageable));
    }

    @PostMapping("/room")
    public ResponseEntity<ChatRoomResponse> createOrGetRoom(@RequestBody ChatRoomDTO chatRoomDTO) {
        ChatRoomResponse room = chatRoomService.findOrCreateRoom(chatRoomDTO);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/room/admin/{adminId}")
    public ResponseEntity<List<ChatRoomResponse>> getAdminRooms(@PathVariable Long adminId) {
        return ResponseEntity.ok(chatRoomService.getActiveRoomsByAdmin(adminId));
    }

    @GetMapping("/room/customer/{customerId}")
    public ResponseEntity<List<ChatRoomResponse>> getCustomerRooms(@PathVariable Long customerId) {
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

    @PutMapping("/messages/room/{roomId}/read/{userId}")
    public ResponseEntity<Integer> markAllAsRead(@PathVariable Long roomId, @PathVariable Long userId) {
        return ResponseEntity.ok(
                chatMessageService.markAllAsRead(roomId, userId)

        );
    }

    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<Void> closeRoom(@PathVariable Long roomId) {
        chatRoomService.markAsInactive(roomId);
        return ResponseEntity.ok().build();
    }


}
