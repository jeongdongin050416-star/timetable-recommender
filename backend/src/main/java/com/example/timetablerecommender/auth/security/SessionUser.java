package com.example.timetablerecommender.auth.security;

import java.io.Serial;
import java.io.Serializable;

public record SessionUser(Long userId, String email, String name) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
