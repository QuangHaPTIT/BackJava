package com.example.ShopApp.controllers;

import com.example.ShopApp.dtos.RefreshTokenDTO;
import com.example.ShopApp.dtos.UserDTO;
import com.example.ShopApp.dtos.UserLoginDTO;
import com.example.ShopApp.entity.Role;
import com.example.ShopApp.entity.User;
import com.example.ShopApp.response.LoginResponse;
import com.example.ShopApp.response.TokenResponse;
import com.example.ShopApp.response.UserResponse;
import com.example.ShopApp.sevices.impl.TokenServiceImpl;
import com.example.ShopApp.sevices.impl.UserSeviceImpl;
import com.example.ShopApp.components.LocalizationUtils;
import com.example.ShopApp.utils.MessageKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;


import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserSeviceImpl userSevice;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;
    private final LocalizationUtils localizationUtils;
    private final TokenServiceImpl tokenService;
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result){
        try{
            if(result.hasErrors()){
                List<String> errorMessage = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList());
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
    private boolean isMobileDevice(String userAgent) {
        // Kiểm tra User-Agent header để xác định thiết bị di động
        // Ví dụ đơn giản:
        return userAgent.toLowerCase().contains("mobile");
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request){
        try {
            String userAgent = request.getHeader("User-Agent");
            String token = userSevice.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword(), userLoginDTO.getRoleId()==null? 1 : userLoginDTO.getRoleId());
            User user = userSevice.getUserDetailsFromToken(token);
            TokenResponse tokenResponse = tokenService.addToken(user.getId(), token, isMobileDevice(userAgent));
            return ResponseEntity.ok(LoginResponse.builder()
                    .message(localizationUtils.getLocalizedMessage("user.login.login_successfully"))
                    .token(token)
                    .refreshToken(tokenResponse.getRefreshToken())
                    .roles(user.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList()))
                    .id(user.getId())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
                    .build()
            );

        }
    }
    @PostMapping("/details")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String authorizationHeader){
        try{
            String token = authorizationHeader.substring(7); //Loại bỏ "Bearer "
            User user = userSevice.getUserDetailsFromToken(token);
            return ResponseEntity.ok(UserResponse.fromUser(user));
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/details/{userId}") // Use the appropriate URL mapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<UserResponse> updateUserDetails(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long userId,
            @RequestBody UserDTO userDTO)
    {
        try{
            String token = authorizationHeader.substring(7);
            User user = userSevice.getUserDetailsFromToken(token);
            if(user.getId() != userId){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            UserResponse updateUser = userSevice.updateUser(userId, userDTO);
            return ResponseEntity.ok(updateUser);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) throws Exception {
        User user = userSevice.getUserDetailsFromToken(refreshTokenDTO.getRefreshToken());
        TokenResponse tokenResponse = tokenService.refreshToken(refreshTokenDTO.getRefreshToken(), user);
        LoginResponse loginResponse = LoginResponse.builder()
                .id(user.getId())
                .token(tokenResponse.getToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .roles(user.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList()))
                .build();
        return ResponseEntity.ok(loginResponse);
    }
}
