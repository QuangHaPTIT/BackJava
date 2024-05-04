package com.example.ShopApp.components;

import com.example.ShopApp.entity.User;
import com.example.ShopApp.exceptions.InvalidParamException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.secretkey}")
    private String secretKey;

    @Value("${jwt.expiration-refresh-token}")
    private int expirationRefreshToken;

    // Vì user trong entity đã implement từ thằng userdetail trong spring security rồi nên tham số là user
    public String generateToken(User user) throws Exception {
        Map<String, Object> claims = new HashMap<>();
        //this.generateSecretKey();
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("userId", user.getId());
        try{
            // Tạo chuỗi JSON Web token từ phonNumber là username
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getPhoneNumber())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                    .signWith(getSignInkey(), SignatureAlgorithm.HS256)
                    .compact();
            return token;
        }catch (Exception e){
            // Có thể dùng logger
            throw new InvalidParamException("Cannot create jwt token, error: " +  e.getMessage());
        }
    }
    public String generateRefreshToken(User user) throws Exception {
        Map<String, Object> claims = new HashMap<>();
        //this.generateSecretKey();
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("userId", user.getId());
        try{
            // Tạo chuỗi JSON Web token từ phonNumber là username
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getPhoneNumber())
                    .setExpiration(new Date(System.currentTimeMillis() + expirationRefreshToken * 1000L))
                    .signWith(getSignInkey(), SignatureAlgorithm.HS256)
                    .compact();
            return token;
        }catch (Exception e){
            // Có thể dùng logger
            throw new InvalidParamException("Cannot create jwt token, error: " +  e.getMessage());
        }
    }

    private Key getSignInkey() {
        byte[] bytes = Decoders.BASE64.decode(secretKey); // Keys.hmacShaKeyFor(Decoders.BASE64.decode("BPrN8FyHleRKz2bGiPi2vnJUwL13otRGmP/bdM9R2Mk="))BPrN8FyHleRKz2bGiPi2vnJUwL13otRGmP/bdM9R2Mk=
        return Keys.hmacShaKeyFor(bytes);
    }
    private String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        String secretKey = Encoders.BASE64.encode(keyBytes);
        return secretKey;
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInkey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    public boolean isTokenExpired(String token) {
        Date expirationDate = this.extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    public String extractPhoneNumber(String token) {
        return this.extractClaim(token, Claims::getSubject);
    }

    // Kiểm tra token còn hạn không và user nhập vào có trùng với sdt không
    public boolean validateToken(String token, UserDetails userDetails) {
        String phoneNumber = extractPhoneNumber(token);
        return (phoneNumber.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
