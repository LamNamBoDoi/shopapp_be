package com.example.shopapp.services.ChatMessage;

import com.example.shopapp.dtos.ChatMessageDTO;
import com.example.shopapp.models.ChatMessage;
import com.example.shopapp.models.ChatRoom;
import com.example.shopapp.repositories.ChatMessageRepository;
import com.example.shopapp.repositories.ChatRoomRepository;
import com.example.shopapp.response.ChatMessageResponse;
import com.example.shopapp.services.ChatMessage.IChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService implements IChatMessageService {
    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    @Override
    public ChatMessageResponse saveMessage(ChatMessageDTO chatMessageDTO) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageDTO.getChatRoomId()).orElseThrow(() -> new DataAccessException("Chat room not found") {
        });

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(chatMessageDTO.getSenderId())
                .receiverId(chatMessageDTO.getReceiverId())
                .senderType(chatMessageDTO.getSenderType())
                .message(chatMessageDTO.getMessage())
                .messageType(chatMessageDTO.getMessageType())
                .read(false) // mới gửi thì chưa đọc
                .build();

        ChatMessage saved = messageRepository.save(message);
        return ChatMessageResponse.fromChatMessage(saved); // cần có hàm này
    }


    @Override
    public List<ChatMessageResponse> getMessagesByRoom(Long chatRoomId) {
        List<ChatMessage> messages = messageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
        return messages.stream()
                .map(ChatMessageResponse::fromChatMessage)
                .toList();
    }

    @Override
    public Page<ChatMessageResponse> getMessagesByRoomPaginated(Long chatRoomId, Pageable pageable) {
        Page<ChatMessage> page = messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);
        return page.map(ChatMessageResponse::fromChatMessage);
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
    public int markAllAsRead(Long roomId, Long userId) {
        List<ChatMessage> unreadMessages = messageRepository.findByChatRoomIdAndReceiverIdAndReadFalse(roomId, userId);
        unreadMessages.forEach(message -> message.setRead(true));
        messageRepository.saveAll(unreadMessages);
        return unreadMessages.size();
    }


}
