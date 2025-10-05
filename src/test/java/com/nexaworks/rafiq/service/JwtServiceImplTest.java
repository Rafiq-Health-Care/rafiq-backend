package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.service.ServiceImpl.JwtServiceImpl;
import static org.junit.jupiter.api.Assertions.*;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.List;


public class JwtServiceImplTest {
    private JwtServiceImpl jwtService;
    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "JWT_SECRET", "5cf7d14433f66174f7ce66b01acef9415f55b04daf4815ad60b62f9c50e8809b");
        ReflectionTestUtils.setField(jwtService, "JWT_EXPIRATION", 10000L);
    }

    @Test
    void generateTokenMustBeNotNull() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("bialy@gmail.com");
        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(user).getAuthorities();

        String token = jwtService.generateToken(user);
        assertNotNull(token);
    }
    @Test
    void jwtMustHaveTheEmail(){
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("bialy@gmail.com");
        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(user).getAuthorities();
        String token = jwtService.generateToken(user);
        Claims claims = Jwts.parser()
                .verifyWith(jwtService.getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertEquals(claims.getSubject(),user.getEmail());
    }
    @Test
    void jwtMustHaveTheClaims(){
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("bialy@gmail.com");
        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(user).getAuthorities();
        String token = jwtService.generateToken(user);
        Claims claims = Jwts.parser()
                .verifyWith(jwtService.getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertEquals(claims.get("authorities"),authorities.stream().map(GrantedAuthority::getAuthority).toList());
    }
}
