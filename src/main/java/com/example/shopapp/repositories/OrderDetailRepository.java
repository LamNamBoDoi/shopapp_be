package com.example.shopapp.repositories;

import com.example.shopapp.models.OrderDetail;
import com.example.shopapp.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);

    @Query("SELECT od.product FROM OrderDetail od WHERE od.order.user.id = :userId")
    List<Product> findProductsByUserId(Long userId);
}
