package com.example.timetablerecommender.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timetablerecommender.auth.dto.AuthResponse;
import com.example.timetablerecommender.auth.dto.LoginRequest;
import com.example.timetablerecommender.auth.dto.SignupRequest;
import com.example.timetablerecommender.auth.service.AuthService;
import com.example.timetablerecommender.auth.security.SessionUser;
import com.example.timetablerecommender.common.api.ApiResponse;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    ResponseEntity<ApiResponse<AuthResponse>> signup(
            @Valid @RequestBody SignupRequest request, HttpServletRequest servletRequest) {
        AuthResponse response = authService.signup(request);
        authenticate(response, servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        AuthResponse response = authService.login(request);
        authenticate(response, servletRequest);
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler()
                .logout(request, response, authentication);
        return ApiResponse.success(null);
    }

    private void authenticate(AuthResponse response, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SessionUser principal = authService.loadSessionUser(response.userId());
        var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, java.util.List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}
