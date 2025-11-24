package com.nexaworks.rafiq.service.ServiceImpl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.TokenType;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.exception.custom.UserException;
import com.nexaworks.rafiq.exception.custom.UserNotFoundException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.JwtService;
import com.nexaworks.rafiq.service.TokenService;

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
        String email = user.getEmail();
        var authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .toList();
        return buildToken(email, authorities);
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
        String email = claims.getSubject();
        return userRepository.findByEmail(email)
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
            var username = claims.getSubject();
            log.info("Validated token for user {}", username);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            log.info("User found {}", user.getEmail());
            List<String> authorities = claims.get("authorities", List.class);
            List<SimpleGrantedAuthority> authorityList = authorities.stream()
                    .map(SimpleGrantedAuthority::new).toList();
            return new UsernamePasswordAuthenticationToken(user, null, authorityList);

        } catch (Exception e) {
            return null;
        }
    }

    private String buildToken(String email, List<String> authorities) {
        return Jwts.builder().subject(email).claim("authorities", authorities).issuer("Rafiq")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getKey()).compact();
    }

    public SecretKey getKey() {
        byte[] key = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(key);
    }
}
