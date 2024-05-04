package com.example.ShopApp.sevices;

import com.example.ShopApp.components.JwtTokenUtil;
import com.example.ShopApp.components.LocalizationUtils;
import com.example.ShopApp.dtos.UserDTO;
import com.example.ShopApp.entity.Role;
import com.example.ShopApp.entity.User;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.exceptions.PermissionDenyException;


import com.example.ShopApp.repositories.RoleRepository;
import com.example.ShopApp.repositories.UserRepository;
import com.example.ShopApp.response.UserResponse;
import com.example.ShopApp.sevices.impl.UserSeviceImpl;
import com.example.ShopApp.utils.MessageKeys;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor

public class UserSevice implements UserSeviceImpl {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; // Của Spring security
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final LocalizationUtils localizationUtils;
    private final ModelMapper modelMapper;
    @Override
    @Transactional
    public UserResponse createUser(UserDTO userDTO) throws Exception {
        // Kiểm tra số điện thoại đã tồn tại hay chưa
        String phoneNumber = userDTO.getPhoneNumber();
        if(userRepository.existsByPhoneNumber(phoneNumber)){
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        Role role = roleRepository.findById(userDTO.getRoleId()).orElseThrow(() -> new DataNotFoundException("Role not found"));
        if(role.getName().toUpperCase().equals(Role.ADMIN)){
            throw new PermissionDenyException("You cannot register an admin account, Please change role's id");
        }
        // convert UserDTO -> User
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId((long) userDTO.getFacebookAccountId())
                .googleAccountId((long) userDTO.getGoogleAccountId())
                .isActive(Boolean.TRUE)
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword())
                .role(role)
                .build();


        // Kiểm tra nếu có accountId thì không yêu cầu password
        if(userDTO.getFacebookAccountId() == 0 && userDTO.getFacebookAccountId() == 0){
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);
        }
        User user =  userRepository.save(newUser);
        UserResponse userResponse = UserResponse.fromUser(user);
        return userResponse;
    }

    @Override
    public String login(String phoneNumber, String password, Long roleId) throws Exception {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if(optionalUser.isEmpty()){
            throw new DataNotFoundException("Invalid phone number / password");
        }
        // return optionalUser.get();

        User existingUser = optionalUser.get();
        if(existingUser.getFacebookAccountId() == 0 && existingUser.getFacebookAccountId() == 0){
            if(!passwordEncoder.matches(password, existingUser.getPassword())){
                throw new BadCredentialsException("Wrong phone number or password");
            }
        }
        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if(optionalRole.isEmpty() || !roleId.equals(existingUser.getRole().getId())){
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED));
        }
        if(!existingUser.isActive()) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                phoneNumber, password, existingUser.getAuthorities()
        );
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(optionalUser.get());
    }

    @Override
    public User getUserDetailsFromToken(String token) throws Exception {
        if(jwtTokenUtil.isTokenExpired(token)) {
            throw new Exception("Token is expired");
        }
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if(user.isPresent()) {
            return user.get();
        }else{
            throw new Exception("User not found");
        }

    }

    @Override
    @Transactional
    public UserResponse updateUser( Long userId, UserDTO userDTO) throws DataNotFoundException {
        User existingUser = userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
        String newPhoneNumber = userDTO.getPhoneNumber();
        if(!existingUser.getPhoneNumber().equals(newPhoneNumber) && userRepository.existsByPhoneNumber(newPhoneNumber)){
            throw new DataIntegrityViolationException("Phone number already exists");
        }


        // Update user infomation based on the DTO
        modelMapper.map(userDTO, existingUser);
        if(userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()){
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                throw new DataNotFoundException("Password and Retype_Password miss match");
            }
            String newPassword = userDTO.getPassword();
            String encodePassword = passwordEncoder.encode(newPassword);
            existingUser.setPassword(encodePassword);
        }
        User userSave = userRepository.save(existingUser);
        return UserResponse.fromUser(userSave);
    }



}
