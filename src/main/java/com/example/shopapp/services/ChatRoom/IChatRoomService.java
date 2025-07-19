package com.example.shopapp.services.ChatRoom;

import com.example.shopapp.models.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface IChatRoomService {
    public Optional<ChatRoom> findByCustomerAndAdmin(Long customerId, Long adminId);
    public ChatRoom findOrCreateRoom(Long customerId, Long adminId);
    public List<ChatRoom> getActiveRoomsByAdmin(Long adminId);
    public List<ChatRoom> getActiveRoomsByCustomer(Long customerId);
    public void markAsInactive(Long roomId);
}
