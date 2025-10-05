package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secret}")
    private  String JWT_SECRET;
    @Value("${jwt.expiration}")
    private  Long JWT_EXPIRATION;

    @Override
    public String generateToken(User user) {
        String email = user.getEmail();
        var authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        return buildToken(email,authorities);
    }

    private String buildToken(String email, List<String> authorities) {
        return Jwts.builder()
                .subject(email)
                .claim("authorities", authorities)
                .issuer("Rafiq")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+JWT_EXPIRATION))
                .signWith(getKey())
                .compact();

    }
   public SecretKey getKey(){
        byte [] key = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(key);
    }
}
