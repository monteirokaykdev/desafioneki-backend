package com.projetoneki.backend.security;

import java.util.Date;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long defaultExpirationMs;

    @Value("${jwt.remember-expiration-ms}")
    private long rememberExpirationMs;

    private SecretKey signingKey;

    // Método chamado após a construção do bean para inicializar a chave de assinatura do JWT
    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(
                java.util.Base64.getEncoder().encode(secret.getBytes())
        );
    }

    public long getDefaultExpirationMs() {
        return defaultExpirationMs;
    }

    public long getRememberExpirationMs() {
        return rememberExpirationMs;
    }

    // Gera um token JWT para o usuário (Admin) com base no ID e email
    public String generateToken(Long adminId, String email, boolean remember) {
        long expirationMs = remember ? rememberExpirationMs : defaultExpirationMs;

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("adminId", adminId)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    //-----FUNÇÕES AUXILIARES-----

    // Extrai o email do token JWT
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrai o ID do Admin do token JWT
    public Long extractAdminId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("adminId", Long.class);
    }

    // Verifica se o token JWT é válido para o email fornecido
    public boolean isTokenValid(String token, String email) {
        String tokenEmail = extractEmail(token);
        return tokenEmail.equals(email) && !isTokenExpired(token);
    }

    // Verifica se o token JWT expirou
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Extrai uma reivindicação específica do token JWT
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extrai todas as reivindicações do token JWT
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
