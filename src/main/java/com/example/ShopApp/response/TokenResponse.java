package com.example.ShopApp.response;

import com.example.ShopApp.entity.Role;
import com.example.ShopApp.entity.Token;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    private Long id;

    private String token;

    private String tokenType;

    @JsonProperty("expiration_date")
    private LocalDateTime expirationDate;

    private boolean revoked;

    private boolean expired;

    @JsonProperty("is_mobile")
    private boolean isMobile;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_expiration_date")
    private LocalDateTime refreshExpirationDate;

    public static TokenResponse fromToken(Token token) {
        return TokenResponse.builder()
                .id(token.getId())
                .token(token.getToken())
                .tokenType(token.getTokenType())
                .expirationDate(token.getExpirationDate())
                .revoked(token.isRevoked())
                .expired(token.isExpired())
                .userId(token.getUser().getId())
                .isMobile(token.isMobile())
                .refreshToken(token.getRefreshToken())
                .refreshExpirationDate(token.getRefreshExpirationDate())
                .build();
    }
}
