package com.example.timetablerecommender.recommendation.dto;

public record RecommendationResponse(
        Long userId,
        int targetCourseCount,
        TimetableResponse timetable) {
}
