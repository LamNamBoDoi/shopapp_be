package com.example.shopapp.repositories;

import com.example.shopapp.models.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(Long chatRoomId);
    Page<ChatMessage> findByChatRoomIdOrderByTimestampDesc(Long chatRoomId, Pageable pageable);
    Long countByReceiverIdAndReadFalse(Long receiverId);
    List<ChatMessage> findByChatRoomIdAndReceiverIdAndReadFalse(Long chatRoomId, Long receiverId);
}
