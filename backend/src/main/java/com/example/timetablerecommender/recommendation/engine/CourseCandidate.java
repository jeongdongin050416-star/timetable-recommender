package com.example.timetablerecommender.recommendation.engine;

import java.util.List;
import java.util.Set;

public record CourseCandidate(
        String courseCode,
        String name,
        int credits,
        boolean majorRequired,
        Set<Long> interestAreaIds,
        Set<String> recommendedPrerequisiteCodes,
        List<SectionCandidate> sections) {

    public CourseCandidate {
        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("courseCode must not be blank");
        }
        interestAreaIds = Set.copyOf(interestAreaIds);
        recommendedPrerequisiteCodes = Set.copyOf(recommendedPrerequisiteCodes);
        sections = List.copyOf(sections);
    }
}
