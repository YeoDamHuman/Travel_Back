package com.example.backend.jwt.config;

import com.example.backend.jwt.dto.JwtDto;
import com.example.backend.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JWTGenerator {

    private final Key key;
    private final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 3000;           // 30분
    private final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7;  // 7일

    public JWTGenerator(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtDto generateToken(User user) {
        Date now = new Date();

        String accessToken = Jwts.builder()
                .setSubject(user.getUserId().toString())      // userId UUID 문자열 변환
                .claim("email", user.getEmail())              // 이메일 클레임 추가 (선택사항)
                .claim("auth", user.getUserRole().name())    // 권한 정보 클레임 추가 (필수)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALIDITY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(user.getUserId().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALIDITY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new JwtDto(accessToken, refreshToken);
    }
}
