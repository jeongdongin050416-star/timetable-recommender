package com.example.timetablerecommender.completedcourse.dto;

public record CompletedCourseItemResponse(
        Long courseId, String courseCode, String name, Integer credits) {
}
