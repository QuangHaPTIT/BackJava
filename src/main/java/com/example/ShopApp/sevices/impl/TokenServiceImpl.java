package com.example.ShopApp.sevices.impl;

import com.example.ShopApp.entity.User;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.response.TokenResponse;

public interface TokenServiceImpl {
    TokenResponse addToken(Long userId, String token, boolean isMobileDevice) throws Exception;
    TokenResponse refreshToken(String refreshToken, User user) throws Exception;
}
