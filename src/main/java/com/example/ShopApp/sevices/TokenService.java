package com.example.ShopApp.sevices;

import com.example.ShopApp.components.JwtTokenUtil;
import com.example.ShopApp.entity.Token;
import com.example.ShopApp.entity.User;
import com.example.ShopApp.exceptions.DataNotFoundException;
import com.example.ShopApp.exceptions.ExpiredTokenException;
import com.example.ShopApp.repositories.TokenRepository;
import com.example.ShopApp.repositories.UserRepository;
import com.example.ShopApp.response.TokenResponse;
import com.example.ShopApp.sevices.impl.TokenServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class TokenService implements TokenServiceImpl {
    private static final int MAX_TOKENS = 3;
    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.expiration-refresh-token}")
    private int expirationRefreshToken;

    private final TokenRepository tokenRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    @Override
    public TokenResponse addToken(Long userId, String token, boolean isMobileDevice) throws Exception {
        List<Token> userTokens = tokenRepository.findByUserId(userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("Not exist user"));
        int tokenSize = userTokens.size();
        if(tokenSize >= MAX_TOKENS) {
            // Kiểm tra xem trong danh sách userTokens có tồn tại ít nhất một token không phải là thiết bị di động (non-mobile)
            boolean hasNonMobileToken = !userTokens.stream().allMatch(Token::isMobile);
            Token tokenToDelete;
            if(hasNonMobileToken) {
                tokenToDelete = userTokens.stream().filter(userToken -> !userToken.isMobile())
                        .findFirst()
                        .orElse(userTokens.get(0));
            }else{
                //tất cả các token đều là thiết bị di động,
                //chúng ta sẽ xóa token đầu tiên trong danh sách
                tokenToDelete = userTokens.get(0);
            }
            tokenRepository.delete(tokenToDelete);
        }
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);
        // Tạo 1 token mới cho người dùng
        Token newToken = Token.builder()
                .user(user)
                .token(token)
                .revoked(false)
                .expired(false)
                .tokenType("Bearer")
                .expirationDate(expirationDateTime)
                .isMobile(isMobileDevice)
                .build();
        newToken.setRefreshToken(jwtTokenUtil.generateRefreshToken(user));
        newToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));
        tokenRepository.save(newToken);
        return TokenResponse.fromToken(newToken);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken, User user) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        if(existingToken == null) {
            throw new DataNotFoundException("Refresh Token does not exist");
        }
        if(existingToken.getRefreshExpirationDate().compareTo(LocalDateTime.now()) < 0) {
            tokenRepository.delete(existingToken);
            throw new ExpiredTokenException("Refresh Token expired");
        }
        String token = jwtTokenUtil.generateToken(user);
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);
        existingToken.setExpirationDate(expirationDateTime);
        existingToken.setToken(token);
        existingToken.setRefreshToken(jwtTokenUtil.generateRefreshToken(user));
        existingToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));
        return TokenResponse.fromToken(existingToken);
    }
}
