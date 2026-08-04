package com.example.timetablerecommender.common.exception;

import org.springframework.http.HttpStatus;

public class InterestAreaNotFoundException extends ApiException {
    public InterestAreaNotFoundException() {
        super(HttpStatus.NOT_FOUND, "INTEREST_AREA_NOT_FOUND", "관심 분야를 찾을 수 없습니다.");
    }
}
