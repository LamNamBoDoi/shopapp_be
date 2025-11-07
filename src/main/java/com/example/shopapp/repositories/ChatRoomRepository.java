package com.example.shopapp.repositories;

import com.example.shopapp.models.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByCustomerIdAndAdminId(Long customerId, Long adminId);
    List<ChatRoom> findByAdminIdAndActiveTrue(Long adminId);
    List<ChatRoom> findByCustomerIdAndActiveTrue(Long customerId);
}
