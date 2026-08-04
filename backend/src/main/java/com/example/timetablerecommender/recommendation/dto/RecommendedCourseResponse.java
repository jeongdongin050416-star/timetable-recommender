package com.example.timetablerecommender.recommendation.dto;

import java.util.List;

public record RecommendedCourseResponse(
        String courseCode,
        String name,
        Integer credits,
        int score,
        List<InterestAreaResponse> interestAreas,
        List<String> reasons) {
}
