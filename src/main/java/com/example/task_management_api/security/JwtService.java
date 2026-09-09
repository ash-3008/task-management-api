package com.example.task_management_api.security;
import com.example.task_management_api.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
@Service public class JwtService {
    private final SecretKey key; private final long expiration;
    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-ms:86400000}") long expiration) { if(secret.length()<32) throw new IllegalArgumentException("JWT secret must contain at least 32 characters"); key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expiration=expiration; }
    public String create(User user) { Date now=new Date(); return Jwts.builder().subject(user.getUsername()).claim("role",user.getRole().name()).issuedAt(now).expiration(new Date(now.getTime()+expiration)).signWith(key).compact(); }
    public String username(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject(); }
    public boolean valid(String token) { try { username(token); return true; } catch (JwtException | IllegalArgumentException ex) { return false; } }
}