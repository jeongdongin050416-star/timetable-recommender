package com.example.timetablerecommender.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timetablerecommender.auth.dto.AuthResponse;
import com.example.timetablerecommender.auth.security.SessionUser;
import com.example.timetablerecommender.common.api.ApiResponse;

@RestController
public class MeController {
    @GetMapping("/api/me")
    ApiResponse<AuthResponse> me(@AuthenticationPrincipal SessionUser user) {
        return ApiResponse.success(new AuthResponse(user.userId(), user.email(), user.name()));
    }
}
