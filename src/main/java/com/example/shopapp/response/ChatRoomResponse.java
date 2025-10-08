package com.example.shopapp.response;

import com.example.shopapp.models.ChatRoom;
import com.example.shopapp.models.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomResponse {
    private Long id;
    @JsonProperty("customer")
    private User customer;

    @JsonProperty("admin")
    private User admin;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    public static ChatRoomResponse fromChatRoom(ChatRoom chatRoom){
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .customer(chatRoom.getCustomer())
                .admin(chatRoom.getAdmin())
                .active(chatRoom.getActive())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}
