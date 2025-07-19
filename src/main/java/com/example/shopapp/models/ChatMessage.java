package com.example.shopapp.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Data
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    private Long senderId;
    private Long receiverId;

    private String senderType; // "CUSTOMER" | "ADMIN"
    private String message; // nội dung hoặc đường dẫn ảnh/video
    private String messageType; // "TEXT", "IMAGE", "VIDEO"
    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Column(name = "is_read")
    private boolean read;
}

