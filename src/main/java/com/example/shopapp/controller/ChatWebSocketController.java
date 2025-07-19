package com.example.shopapp.controller;

import com.example.shopapp.models.ChatMessage;
import com.example.shopapp.models.ChatRoom;
import com.example.shopapp.services.ChatMessage.IChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
// lớp xử lý tin nhắn
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final IChatMessageService chatMessageService;
    // giúp gửi dữ liệu từ server đến client
    private final SimpMessagingTemplate messagingTemplate;

    // bắt các message gửi đến theo cấu hình
    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload ChatMessage message){
        message.setRead(false);
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(roomId);
        message.setChatRoom(chatRoom);

        ChatMessage savedMessage = chatMessageService.saveMessage(message);

        // Gửi tin nhắn đến room cụ thể
        messagingTemplate.convertAndSend("/topic/room/" + roomId, savedMessage);

        // Thông báo cho admin/customer về tin nhắn mới
        messagingTemplate.convertAndSend("/topic/notifications/" + message.getReceiverId(),
                Map.of("type", "new_message", "roomId", roomId, "message", savedMessage));
    }

    // gửi sự kiện đang gõ
    // server chuyển tiếp tới các user khác
    @MessageMapping("/chat.typing/{roomId}")
    public void handleTyping(@DestinationVariable Long roomId, @Payload Map<String, Object> typingData) {
        messagingTemplate.convertAndSend("/topic/typing/" + roomId, typingData);
    }

    // khi user join vào room, client gửi thông tin lên
    @MessageMapping("/chat.join/{roomId}")
    public void joinRoom(@DestinationVariable Long roomId, @Payload Map<String, Long> userData) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/users", userData);
    }

    // khi user rời phòng chat
    @MessageMapping("/chat.leave/{roomId}")
    public void leaveRoom(@DestinationVariable Long roomId, @Payload Map<String, Long> userData) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/users", userData);
    }
}
