package com.example.shopapp.services.ChatMessage;

import com.example.shopapp.models.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IChatMessageService {
    public ChatMessage saveMessage(ChatMessage chatMessage);
    public List<ChatMessage> getMessagesByRoom(Long chatRoomId);
    public Page<ChatMessage> getMessagesByRoomPaginated(Long chatRoomId, Pageable pageable);
    public void markAsRead(Long messageId);
    public Long getUnreadCount(Long userId);
    public void markAllAsRead(Long roomId, Long userId);
}
