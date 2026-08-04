package com.example.timetablerecommender.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateUserException extends ApiException {
    public DuplicateUserException() {
        super(HttpStatus.CONFLICT, "DUPLICATE_USER", "이미 사용 중인 이메일입니다.");
    }
}
