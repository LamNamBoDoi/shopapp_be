package com.example.shopapp.services.ChatRoom;

import com.example.shopapp.models.ChatRoom;
import com.example.shopapp.repositories.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService implements IChatRoomService{
    private final ChatRoomRepository chatRoomRepository;
    @Override
    public Optional<ChatRoom> findByCustomerAndAdmin(Long customerId, Long adminId) {
        return chatRoomRepository.findByCustomerIdAndAdminId(customerId, adminId);
    }

    @Override
    public ChatRoom findOrCreateRoom(Long customerId, Long adminId) {
        return chatRoomRepository.findByCustomerIdAndAdminId(customerId, adminId).orElseGet(()->{
            ChatRoom room = new ChatRoom();
            room.setCustomerId(customerId);
            room.setAdminId(customerId);
            room.setActive(true);
            room.setCreatedAt(LocalDateTime.now());
            return chatRoomRepository.save(room);
        });
    }

    @Override
    public List<ChatRoom> getActiveRoomsByAdmin(Long adminId) {
        return chatRoomRepository.findByAdminIdAndActiveTrue(adminId);
    }
    @Override
    public List<ChatRoom> getActiveRoomsByCustomer(Long customerId) {
        return chatRoomRepository.findByCustomerIdAndActiveTrue(customerId);
    }

    @Override
    public void markAsInactive(Long roomId) {
        chatRoomRepository.findById(roomId).ifPresent(room -> {
            room.setActive(false);
            chatRoomRepository.save(room);
        });
    }
}
