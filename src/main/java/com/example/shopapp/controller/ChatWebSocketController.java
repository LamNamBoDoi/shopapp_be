package com.example.shopapp.controller;

import com.example.shopapp.dtos.ChatMessageDTO;
import com.example.shopapp.models.ChatMessage;
import com.example.shopapp.models.ChatRoom;
import com.example.shopapp.response.ChatMessageResponse;
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
    public void sendMessage(@DestinationVariable Long roomId, @Payload ChatMessageDTO chatMessageDTO){
        ChatMessageResponse savedMessage = chatMessageService.saveMessage(chatMessageDTO);

        // Gửi tin nhắn đến room cụ thể
        messagingTemplate.convertAndSend("/topic/room/" + roomId, savedMessage);

        // Thông báo cho admin/customer về tin nhắn mới
        messagingTemplate.convertAndSend("/topic/notifications/" + chatMessageDTO.getReceiverId(),
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
