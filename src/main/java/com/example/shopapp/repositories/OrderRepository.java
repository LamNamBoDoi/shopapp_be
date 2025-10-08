package com.example.shopapp.repositories;

import com.example.shopapp.models.Order;
import com.example.shopapp.utils.ConfixSql;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Tim cac don hang cua 1 user nao do
    List<Order> findByUserId(Long userId);
    // lẩy ra tất cả các order
    @Query(ConfixSql.Order.GET_ALL_ORDER)
    Page<Order> findByKeyword(String keyword, Pageable pageable);
    List<Order> findAllByActiveTrue();
}
