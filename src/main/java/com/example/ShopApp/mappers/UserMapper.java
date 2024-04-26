package com.example.ShopApp.mappers;

import com.example.ShopApp.dtos.UserDTO;
import com.example.ShopApp.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    public User fromUser(UserDTO userDTO);
}
