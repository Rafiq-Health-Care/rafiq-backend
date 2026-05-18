package com.nexaworks.rafiq.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.user.UserException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.authentication.JwtServiceImpl;
import com.nexaworks.rafiq.service.user.TokenServiceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@DisplayName("JwtServiceImpl Test")
public class JwtServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    TokenServiceImpl tokenServiceImpl;
    @InjectMocks
    JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jwtService, "JWT_SECRET",
                "5cf7d14433f66174f7ce66b01acef9415f55b04daf4815ad60b62f9c50e8809b");
        ReflectionTestUtils.setField(jwtService, "JWT_EXPIRATION", 10000L);
    }

    @DisplayName("Generated token should not be null")
    @Test
    void generateTokenMustBeNotNull() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("bialy@gmail.com");
        Collection<? extends GrantedAuthority> authorities = List
                .of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(user).getAuthorities();

        String token = jwtService.generateToken(user);
        assertNotNull(token);
    }

    @DisplayName("Generated token must have user id")
    @Test
    void jwtMustHaveTheEmail() {
        User user = mock(User.class);
        UUID id = UUID.randomUUID();
        when(user.getId()).thenReturn(id);
        Collection<? extends GrantedAuthority> authorities = List
                .of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(user).getAuthorities();
        String token = jwtService.generateToken(user);
        Claims claims = Jwts.parser().verifyWith(jwtService.getKey()).build()
                .parseSignedClaims(token).getPayload();
        assertEquals(claims.getSubject(), id.toString());
    }

    @DisplayName("Generated token must have the claims")
    @Test
    void jwtMustHaveTheClaims() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("bialy@gmail.com");
        Collection<? extends GrantedAuthority> authorities = List
                .of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(user).getAuthorities();
        String token = jwtService.generateToken(user);
        Claims claims = Jwts.parser().verifyWith(jwtService.getKey()).build()
                .parseSignedClaims(token).getPayload();
        assertEquals(claims.get("authorities"),
                authorities.stream().map(GrantedAuthority::getAuthority).toList());
    }

    @DisplayName("Generate token with null user should throw exception")
    @Test
    void generateTokenWithNullUserShouldThrowException() {
        assertThrows(UserException.class, () -> jwtService.generateToken(null));
    }
}
