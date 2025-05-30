package com.example.shopapp.repositories;

import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByToken(String token);
    Optional<Token> findByUser_Id(Long userId);
    List<Token> findByUser(User user);
    Optional<Token> findByRefreshToken(String refreshToken);
}
