package com.example.timetablerecommender.common.exception;

import org.springframework.http.HttpStatus;

public class CourseNotFoundException extends ApiException {
    public CourseNotFoundException() {
        super(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "과목을 찾을 수 없습니다.");
    }
}
