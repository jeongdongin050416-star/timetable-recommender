package com.example.timetablerecommender.completedcourse.dto;

public record CompletedCourseStatusResponse(Long userId, String courseCode, boolean completed) {
}
