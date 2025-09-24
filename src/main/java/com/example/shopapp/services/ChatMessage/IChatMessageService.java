package com.example.shopapp.services.ChatMessage;

import com.example.shopapp.dtos.ChatMessageDTO;
import com.example.shopapp.models.ChatMessage;
import com.example.shopapp.response.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IChatMessageService {
    public ChatMessageResponse saveMessage(ChatMessageDTO chatMessageDTO);
    public List<ChatMessageResponse> getMessagesByRoom(Long chatRoomId);
    public Page<ChatMessageResponse> getMessagesByRoomPaginated(Long chatRoomId, Pageable pageable);
    public void markAsRead(Long messageId);
    public Long getUnreadCount(Long userId);
    public int markAllAsRead(Long roomId, Long userId);
}
