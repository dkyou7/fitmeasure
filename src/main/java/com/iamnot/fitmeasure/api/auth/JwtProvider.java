package com.iamnot.fitmeasure.api.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long validityMillis;

    public JwtProvider(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.validity-days:30}") long validityDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityMillis = validityDays * 24 * 60 * 60 * 1000L;
    }

    public String issue(Long memberId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validityMillis))
                .signWith(key)
                .compact();
    }

    /** 유효하면 memberId, 아니면 null */
    public Long parseMemberId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }
}