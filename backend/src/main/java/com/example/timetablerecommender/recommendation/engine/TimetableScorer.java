package com.example.timetablerecommender.recommendation.engine;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimetableScorer {

    static final int INTERESTED_COURSE_SCORE = 2;
    static final int UNINTERESTED_COURSE_SCORE = -2;
    static final int MAJOR_REQUIRED_SCORE = 5;
    static final int RECOMMENDED_PREREQUISITE_MET_SCORE = 3;
    static final int RECOMMENDED_PREREQUISITE_UNMET_SCORE = 0;
    static final int PREREQUISITE_MET_SCORE = 3;
    static final int PREREQUISITE_UNMET_SCORE = -3;
    private static final Pattern COURSE_NUMBER_PATTERN = Pattern.compile("(?:^|\\D)(\\d{3})$");

    public int score(List<TimetableSelection> selections, RecommendationCriteria criteria) {
        int score = 0;
        for (TimetableSelection selection : selections) {
            CourseCandidate course = selection.course();
            if (course.interestAreaIds().stream().anyMatch(criteria.interestedAreaIds()::contains)) {
                score += INTERESTED_COURSE_SCORE;
            }
            if (course.interestAreaIds().stream().anyMatch(criteria.uninterestedAreaIds()::contains)) {
                score += UNINTERESTED_COURSE_SCORE;
            }
            if (course.majorRequired()) {
                score += MAJOR_REQUIRED_SCORE;
            }
            score += studentYearScore(course.courseCode(), criteria.studentYear());
            for (String prerequisite : course.recommendedPrerequisiteCodes()) {
                score += criteria.completedCourseCodes().contains(prerequisite)
                        ? RECOMMENDED_PREREQUISITE_MET_SCORE
                        : RECOMMENDED_PREREQUISITE_UNMET_SCORE;
            }
            for (String prerequisite : course.prerequisiteCodes()) {
                score += criteria.completedCourseCodes().contains(prerequisite)
                        ? PREREQUISITE_MET_SCORE
                        : PREREQUISITE_UNMET_SCORE;
            }
        }
        return score;
    }

    private int studentYearScore(String courseCode, StudentYear studentYear) {
        Matcher matcher = COURSE_NUMBER_PATTERN.matcher(courseCode);
        if (!matcher.find()) {
            return 0;
        }

        int courseLevel = Integer.parseInt(matcher.group(1)) / 100;
        return switch (studentYear) {
            case FIRST_YEAR -> switch (courseLevel) {
                case 1 -> 15;
                case 2 -> 10;
                default -> 0;
            };
            case SECOND_YEAR -> switch (courseLevel) {
                case 2 -> 15;
                case 3 -> 10;
                default -> 0;
            };
            case THIRD_YEAR -> switch (courseLevel) {
                case 2 -> 10;
                case 3 -> 15;
                case 4 -> 5;
                default -> 0;
            };
            case FOURTH_YEAR_OR_ABOVE -> switch (courseLevel) {
                case 3 -> 14;
                case 4 -> 15;
                default -> 0;
            };
        };
    }

}
