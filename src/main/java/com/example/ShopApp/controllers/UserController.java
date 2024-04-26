package com.example.ShopApp.controllers;

import com.example.ShopApp.dtos.UserDTO;
import com.example.ShopApp.dtos.UserLoginDTO;
import com.example.ShopApp.entity.Role;
import com.example.ShopApp.entity.User;
import com.example.ShopApp.response.LoginResponse;
import com.example.ShopApp.response.UserResponse;
import com.example.ShopApp.sevices.impl.UserSeviceImpl;
import com.example.ShopApp.components.LocalizationUtils;
import com.example.ShopApp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;


import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserSeviceImpl userSevice;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;
    private final LocalizationUtils localizationUtils;
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result){
        try{
            if(result.hasErrors()){
                List<String> errorMessage = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }

            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
                return ResponseEntity.badRequest().body(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH));
            }

            UserResponse userResponse = userSevice.createUser(userDTO);
            return ResponseEntity.ok(userResponse);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginDTO userLoginDTO){
        try {
            String token = userSevice.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword(), userLoginDTO.getRoleId()==null? 1 : userLoginDTO.getRoleId());
            return ResponseEntity.ok(LoginResponse.builder()
                    .message(localizationUtils.getLocalizedMessage("user.login.login_successfully"))
                    .token(token)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
                    .build()
            );

        }
    }
    @PostMapping("/details")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String authorizationHeader){
        try{
            String token = authorizationHeader.substring(7); //Loại bỏ "Bearer "
            UserResponse userResponse = userSevice.getUserDetailsFromToken(token);
            return ResponseEntity.ok(userResponse);
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/details/{userId}") // Use the appropriate URL mapping
    public ResponseEntity<UserResponse> updateUserDetails(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long userId,
            @RequestBody UserDTO userDTO)
    {
        try{
            String token = authorizationHeader.substring(7);
            UserResponse user = userSevice.getUserDetailsFromToken(token);
            if(user.getId() != userId){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            UserResponse updateUser = userSevice.updateUser(userId, userDTO);
            return ResponseEntity.ok(updateUser);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
