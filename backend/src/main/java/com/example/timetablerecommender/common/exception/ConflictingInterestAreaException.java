package com.example.timetablerecommender.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictingInterestAreaException extends ApiException {
    public ConflictingInterestAreaException() {
        super(HttpStatus.BAD_REQUEST, "CONFLICTING_INTEREST_AREA", "관심 분야와 제외 분야는 겹칠 수 없습니다.");
    }
}
