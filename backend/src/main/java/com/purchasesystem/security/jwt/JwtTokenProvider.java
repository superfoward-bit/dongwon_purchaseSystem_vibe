package com.purchasesystem.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성/검증.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms}") long accessValidityMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessValidityMs = accessValidityMs;
    }

    public String createAccessToken(String loginId, String compCd, String usrId, String usrNm, String vdCd) {
        Date now = new Date();
        return Jwts.builder()
                .subject(loginId)
                .claim("compCd", compCd)
                .claim("usrId", usrId)
                .claim("usrNm", usrNm)
                .claim("vdCd", vdCd)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessValidityMs))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
