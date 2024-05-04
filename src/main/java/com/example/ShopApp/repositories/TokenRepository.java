package com.example.ShopApp.repositories;

import com.example.ShopApp.entity.Token;
import com.example.ShopApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByUserId(Long userId);
    Token findByToken(String token);
    Token findByRefreshToken(String token);
}
