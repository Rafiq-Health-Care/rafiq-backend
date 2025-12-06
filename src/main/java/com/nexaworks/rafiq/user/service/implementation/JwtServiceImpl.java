package com.nexaworks.rafiq.user.service.implementation;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.user.entity.enums.TokenType;
import com.nexaworks.rafiq.user.entity.model.Token;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.exception.TokenInvalidException;
import com.nexaworks.rafiq.user.exception.UserException;
import com.nexaworks.rafiq.user.exception.UserNotFoundException;
import com.nexaworks.rafiq.user.repository.UserRepository;
import com.nexaworks.rafiq.user.service.JwtService;
import com.nexaworks.rafiq.user.service.TokenService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Value("${jwt.expiration}")
    private Long JWT_EXPIRATION;

    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Override
    public String generateToken(User user) {
        if (user == null) {
            throw new UserException("User cannot be null");
        }
        UUID id = user.getId();
        var authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .toList();
        return buildToken(id, authorities);
    }

    @Override
    public void invalidateJwtToken(String s) {
        Token token = Token.builder().token(s).tokenType(TokenType.JWT_BLACKLIST)
                .expiryDate(getExpirationDate(s)).user(getUser(s)).build();
        tokenService.saveToken(token);

    }

    private User getUser(String s) {
        Claims claims = Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(s)
                .getPayload();
        UUID userId = UUID.fromString(claims.getSubject());
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Instant getExpirationDate(String s) {
        Claims claims = Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(s)
                .getPayload();
        Date expiration = claims.getExpiration();
        return expiration.toInstant();
    }

    @Override
    public Authentication validate(String jwt) {
        try {
            Claims claims = Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(jwt)
                    .getPayload();
            if (claims.getExpiration().before(new Date())) {
                throw new TokenInvalidException("Token has expired");
            }
            if (claims.get("authorities") == null) {
                throw new BadCredentialsException("Token has no authorities");
            }
            UUID userId = UUID.fromString(claims.getSubject());
            List<String> authorities = claims.get("authorities", List.class);
            List<SimpleGrantedAuthority> authorityList = authorities.stream()
                    .map(SimpleGrantedAuthority::new).toList();
            return new UsernamePasswordAuthenticationToken(userId, null, authorityList);

        } catch (Exception e) {
            return null;
        }
    }

    private String buildToken(UUID userId, List<String> authorities) {
        return Jwts.builder().subject(String.valueOf(userId)).claim("authorities", authorities)
                .issuer("Rafiq").issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getKey()).compact();
    }

    public SecretKey getKey() {
        byte[] key = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(key);
    }
}
