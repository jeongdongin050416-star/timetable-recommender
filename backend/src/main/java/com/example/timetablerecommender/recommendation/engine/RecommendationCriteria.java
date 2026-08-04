package com.example.timetablerecommender.recommendation.engine;

import java.util.Set;

public record RecommendationCriteria(
        int targetCourseCount,
        Set<Long> interestedAreaIds,
        Set<Long> uninterestedAreaIds,
        Set<String> completedCourseCodes) {

    public RecommendationCriteria {
        if (targetCourseCount < 1) {
            throw new IllegalArgumentException("targetCourseCount must be positive");
        }
        interestedAreaIds = Set.copyOf(interestedAreaIds);
        uninterestedAreaIds = Set.copyOf(uninterestedAreaIds);
        completedCourseCodes = Set.copyOf(completedCourseCodes);
    }
}
