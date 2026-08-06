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
        Set<String> prerequisiteCodes,
        Set<String> incompatibleCourseCodes,
        List<SectionCandidate> sections) {

    public CourseCandidate {
        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("courseCode must not be blank");
        }
        interestAreaIds = Set.copyOf(interestAreaIds);
        recommendedPrerequisiteCodes = Set.copyOf(recommendedPrerequisiteCodes);
        prerequisiteCodes = Set.copyOf(prerequisiteCodes);
        incompatibleCourseCodes = Set.copyOf(incompatibleCourseCodes);
        sections = List.copyOf(sections);
    }

    public CourseCandidate(
            String courseCode,
            String name,
            int credits,
            boolean majorRequired,
            Set<Long> interestAreaIds,
            Set<String> recommendedPrerequisiteCodes,
            Set<String> prerequisiteCodes,
            List<SectionCandidate> sections) {
        this(courseCode, name, credits, majorRequired, interestAreaIds,
                recommendedPrerequisiteCodes, prerequisiteCodes, Set.of(), sections);
    }

    public CourseCandidate(
            String courseCode,
            String name,
            int credits,
            boolean majorRequired,
            Set<Long> interestAreaIds,
            Set<String> recommendedPrerequisiteCodes,
            List<SectionCandidate> sections) {
        this(courseCode, name, credits, majorRequired, interestAreaIds,
                recommendedPrerequisiteCodes, Set.of(), Set.of(), sections);
    }
}
