package com.example.shopapp.services.ChatRoom;

import com.example.shopapp.dtos.ChatRoomDTO;
import com.example.shopapp.models.ChatRoom;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.ChatRoomRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.response.ChatRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService implements IChatRoomService{
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    @Override
    public Optional<ChatRoomResponse> findByCustomerAndAdmin(Long customerId, Long adminId) {
        return chatRoomRepository
                .findByCustomerIdAndAdminId(customerId, adminId)
                .map(ChatRoomResponse::fromChatRoom);
    }


    @Override
    public ChatRoomResponse findOrCreateRoom(ChatRoomDTO chatRoomDTO) {
        ChatRoom chatRoom = chatRoomRepository
                .findByCustomerIdAndAdminId(chatRoomDTO.getCustomerId(), chatRoomDTO.getAdminId())
                .orElseGet(() -> {
                    User customer = userRepository.findById(chatRoomDTO.getCustomerId()).orElseThrow();
                    User admin = userRepository.findById(chatRoomDTO.getAdminId()).orElseThrow();
                    ChatRoom room = ChatRoom.builder()
                            .customer(customer)
                            .admin(admin)
                            .active(chatRoomDTO.getActive())
                            .build();
                    return chatRoomRepository.save(room); // return ChatRoom
                });

        return ChatRoomResponse.fromChatRoom(chatRoom); // convert to DTO
    }


    @Override
    public List<ChatRoomResponse> getActiveRoomsByAdmin(Long adminId) {
        return chatRoomRepository.findByAdminIdAndActiveTrue(adminId)
                .stream()
                .map(ChatRoomResponse::fromChatRoom)
                .toList();
    }

    @Override
    public List<ChatRoomResponse> getActiveRoomsByCustomer(Long customerId) {
        return chatRoomRepository.findByCustomerIdAndActiveTrue(customerId)
                .stream()
                .map(ChatRoomResponse::fromChatRoom)
                .toList();
    }


    @Override
    public void markAsInactive(Long roomId) {
        chatRoomRepository.findById(roomId).ifPresent(room -> {
            room.setActive(false);
            chatRoomRepository.save(room);
        });
    }
}
