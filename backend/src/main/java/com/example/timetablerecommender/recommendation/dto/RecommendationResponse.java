package com.example.timetablerecommender.recommendation.dto;

import java.util.List;

public record RecommendationResponse(
        Long userId,
        int requestedCourseCount,
        int returnedCourseCount,
        List<RecommendedCourseResponse> courses) {
}
