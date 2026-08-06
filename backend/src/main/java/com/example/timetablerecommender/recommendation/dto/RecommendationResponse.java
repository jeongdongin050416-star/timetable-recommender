package com.example.timetablerecommender.recommendation.dto;

import com.example.timetablerecommender.recommendation.engine.StudentYear;

public record RecommendationResponse(
        Long userId,
        int targetCourseCount,
        StudentYear studentYear,
        TimetableResponse timetable) {
}
