package com.almetpt.coursework.bookclub.config.jwt;

import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetails; // Убедитесь, что импорт есть
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority; // Для извлечения роли
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration; // в секундах

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // Методы для извлечения пользовательских claims из токена (если потребуется на бэкенде)
    public Integer getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", Integer.class));
    }

    public String getUserRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userRole", String.class));
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        // Jwts.parserBuilder() автоматически обрабатывает ExpiredJwtException, MalformedJwtException и т.д.
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        try {
            final Date expirationDate = getExpirationDateFromToken(token);
            return expirationDate.before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true; // Если сам парсинг токена вызвал исключение истечения срока
        }
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            claims.put("userId", customUserDetails.getUserId()); // userId уже Integer в CustomUserDetails
            // Извлекаем строку роли, например, "USER", "ADMIN"
            String role = customUserDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth) // Убираем префикс "ROLE_"
                    .findFirst() // Предполагаем одну основную роль для простоты в этом claim
                    .orElse("USER"); // Значение по умолчанию или обработка по необходимости
            claims.put("userRole", role);
        }
        return createToken(claims, userDetails.getUsername()); // userDetails.getUsername() это email
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (io.jsonwebtoken.JwtException e) { // Ловим общие JWT исключения (Expired, Malformed, etc.)
            // logger.warn("Token validation error: {}", e.getMessage()); // Если есть logger
            return false;
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
