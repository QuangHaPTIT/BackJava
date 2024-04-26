package com.example.ShopApp.dtos;

import com.example.ShopApp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessage {
    private String message;
    private User user;
}
