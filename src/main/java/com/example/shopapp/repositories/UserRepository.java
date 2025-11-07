package com.example.shopapp.repositories;

import com.example.shopapp.models.User;
import com.example.shopapp.utils.ConfixSql;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByPhoneNumber(String phoneNumber);

    //lấy ra tất cả user(ngoại trừ admin) vưới truyền admin
    @Query(ConfixSql.User.GET_ALL_USER)
    Page<User> findAll(@Param("keyword") String keyword, Pageable pageable);
    List<User> findAll();
}
