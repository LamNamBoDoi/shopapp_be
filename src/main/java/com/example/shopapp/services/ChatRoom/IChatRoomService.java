package com.example.shopapp.services.ChatRoom;

import com.example.shopapp.dtos.ChatRoomDTO;
import com.example.shopapp.models.ChatRoom;
import com.example.shopapp.response.ChatRoomResponse;

import java.util.List;
import java.util.Optional;

public interface IChatRoomService {
    public Optional<ChatRoomResponse> findByCustomerAndAdmin(Long customerId, Long adminId);
    public ChatRoomResponse findOrCreateRoom(ChatRoomDTO chatRoomDTO);
    public List<ChatRoomResponse> getActiveRoomsByAdmin(Long adminId);
    public List<ChatRoomResponse> getActiveRoomsByCustomer(Long customerId);
    public void markAsInactive(Long roomId);
}
