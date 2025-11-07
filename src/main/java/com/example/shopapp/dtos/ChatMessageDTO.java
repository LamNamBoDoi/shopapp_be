package com.example.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("chat_room_id")
    private Long chatRoomId;

    @JsonProperty("sender_id")
    private Long senderId;

    @JsonProperty("receiver_id")
    private Long receiverId;

    @JsonProperty("sender_type")
    private String senderType; // "CUSTOMER" | "ADMIN"

    @JsonProperty("message")
    private String message;

    @JsonProperty("message_type")
    private String messageType; // "TEXT", "IMAGE", "VIDEO"

    @JsonProperty("is_read")
    private boolean read;
}
