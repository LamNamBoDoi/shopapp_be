package com.example.shopapp.repositories;

import com.example.shopapp.models.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserId(Long userId);
    List<Wishlist> findByProductId(Long productId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}

