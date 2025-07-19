package com.example.shopapp.services;

import com.example.shopapp.models.ChatMessage;
import com.example.shopapp.repositories.ChatMessageRepository;
import com.example.shopapp.services.ChatMessage.IChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService implements IChatMessageService {
    private final ChatMessageRepository messageRepository;

    public ChatMessage saveMessage(ChatMessage message) {
        return messageRepository.save(message);
    }

    public List<ChatMessage> getMessagesByRoom(Long chatRoomId) {
        return messageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
    }

    @Override
    public Page<ChatMessage> getMessagesByRoomPaginated(Long chatRoomId, Pageable pageable) {
        return messageRepository.findByChatRoomIdOrderByTimestampDesc(chatRoomId, pageable);
    }

    @Override
    public void markAsRead(Long messageId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            message.setRead(true);
            messageRepository.save(message);
        });
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return messageRepository.countByReceiverIdAndReadFalse(userId);
    }

    @Override
    public void markAllAsRead(Long roomId, Long userId) {
        List<ChatMessage> unreadMessages = messageRepository.findByChatRoomIdAndReceiverIdAndReadFalse(roomId, userId);
        unreadMessages.forEach(message -> message.setRead(true));
        messageRepository.saveAll(unreadMessages);
    }
}
