package com.nexaworks.rafiq.config;

import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.service.JwtService;
import com.nexaworks.rafiq.service.ServiceImpl.TokenServiceImpl;
import com.nexaworks.rafiq.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.DelegatingServletOutputStream;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2SuccessHandlerTest {
    @Mock
    TokenServiceImpl tokenService;
    @Mock
    private User user;
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Authentication authentication;
    @Mock
    private OAuth2User oAuth2User;
    private ByteArrayOutputStream outputStream;


    void setUp() throws IOException {
        outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(outputStream));
    }
    @DisplayName("Should return 400 when email is null")
    @Test
    void shouldReturn400WhenEmailIsNull()throws Exception{
        setUp();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(null);
        when(oAuth2User.getAttribute("name")).thenReturn("Bialy");

        customOAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(outputStream.toString().contains("Cannot find email"));
    }

    @DisplayName("Should return 200 when email is present")
    @Test
    void shouldReturn200WhenEmailIsPresent()throws Exception{
        setUp();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("");
        when(oAuth2User.getAttribute("name")).thenReturn("Bialy");
        customOAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @DisplayName("Should throw exception when user is disabled")
    @Test
    void shouldThrowExceptionWhenUserIsDisabled()throws Exception{
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("");
        when(oAuth2User.getAttribute("name")).thenReturn("Bialy");

        when(userService.findByEmail("")).thenReturn(Optional.of(user));
        when(user.isEnabled()).thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
                customOAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication)
        );
    }

    @DisplayName("Should have user data if user is not present")
    @Test
    void shouldHaveUserDataIfUserIsNotPresent()throws Exception{
        setUp();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("bialy@gmail.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Bialy");

        when(userService.findByEmail("bialy@gmail.com")).thenReturn(Optional.empty());
        customOAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(outputStream.toString().contains("Bialy"));
        assertTrue(outputStream.toString().contains("bialy@gmail.com"));
    }

    @DisplayName("Should have jwt if user if present and is not disabled")
    @Test
    void shouldHaveJwtIfUserIfPresentAndIsEnabled() throws IOException, ServletException {
        setUp();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("bialy@gmail.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Bialy");
        when(userService.findByEmail("bialy@gmail.com")).thenReturn(Optional.of(user));
        when(user.isEnabled()).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("token");
        when(user.getRoles()).thenReturn(List.of(Role.builder().name("ROLE_USER").build()));
        when(tokenService.generateRefreshToken(user)).thenReturn("refreshToken");
        customOAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(outputStream.toString().contains("token"));
        assertTrue(outputStream.toString().contains("refreshToken"));
        assertTrue(outputStream.toString().contains("ROLE_USER"));



    }
}
