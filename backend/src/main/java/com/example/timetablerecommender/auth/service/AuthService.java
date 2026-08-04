package com.example.timetablerecommender.auth.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timetablerecommender.auth.dto.AuthResponse;
import com.example.timetablerecommender.auth.dto.LoginRequest;
import com.example.timetablerecommender.auth.dto.SignupRequest;
import com.example.timetablerecommender.common.exception.DuplicateUserException;
import com.example.timetablerecommender.common.exception.InvalidCredentialsException;
import com.example.timetablerecommender.domain.AppUser;
import com.example.timetablerecommender.repository.AppUserRepository;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByLoginId(email)) {
            throw new DuplicateUserException();
        }
        try {
            AppUser user = userRepository.saveAndFlush(new AppUser(
                    email, passwordEncoder.encode(request.password()), request.name().trim()));
            return toResponse(user);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUserException();
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByLoginId(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return toResponse(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private AuthResponse toResponse(AppUser user) {
        return new AuthResponse(user.getId(), user.getLoginId(), user.getName());
    }
}
