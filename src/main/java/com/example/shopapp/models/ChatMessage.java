package com.example.shopapp.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chat_messages")
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(name = "sender_type")
    private String senderType; // "CUSTOMER" | "ADMIN"

    @Column(name = "message")
    private String message; // nội dung hoặc đường dẫn ảnh/video

    @Column(name = "message_type")
    private String messageType; // "TEXT", "IMAGE", "VIDEO"

    @Column(name = "is_read")
    private Boolean read;
}

