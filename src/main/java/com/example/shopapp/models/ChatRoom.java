package com.example.shopapp.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chat_rooms")
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Từ Long -> User (quan hệ nhiều phòng chat thuộc 1 user)
    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "admin_id", referencedColumnName = "id")
    private User admin;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> messages;

    @Column(name = "active")
    private Boolean active;
}


