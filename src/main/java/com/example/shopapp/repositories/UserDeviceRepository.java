package com.example.shopapp.repositories;

import com.example.shopapp.models.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    List<UserDevice> findByUserIdAndIsActiveTrue(Long userId);

    @Modifying
    @Query("UPDATE UserDevice d SET d.isActive = false WHERE d.deviceToken = :token")
    int deactivateByToken(@Param("token") String token);

    Optional<UserDevice> findByDeviceToken(String deviceToken);
}
