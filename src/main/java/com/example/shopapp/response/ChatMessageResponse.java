package com.example.shopapp.response;

import com.example.shopapp.models.ChatMessage;
import com.example.shopapp.models.ChatRoom;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;

    @JsonProperty("chat_room_id")
    private Long chatRoomId;

    @JsonProperty("sender_id")
    private Long senderId;

    @JsonProperty("receiver_id")
    private Long receiverId;

    @JsonProperty("sender_type")
    private String senderType;

    @JsonProperty("message")
    private String message;

    @JsonProperty("message_type")
    private String messageType;

    @JsonProperty("is_read")
    private boolean read;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ChatMessageResponse fromChatMessage(ChatMessage chatMessage){
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .senderId(chatMessage.getSenderId())
                .receiverId(chatMessage.getReceiverId())
                .senderType(chatMessage.getSenderType())
                .message(chatMessage.getMessage())
                .messageType(chatMessage.getMessageType())
                .read(chatMessage.getRead())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}
