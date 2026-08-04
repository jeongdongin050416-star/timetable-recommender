package com.example.timetablerecommender.recommendation.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.timetablerecommender.domain.Course;

@Component
public class RecommendationPolicy {

    private static final int INTEREST_MATCH_SCORE = 100;
    private static final int MAJOR_REQUIRED_SCORE = 10;
    private static final String MAJOR_REQUIRED = "MAJOR_REQUIRED";

    public ScoreResult score(Course course, Set<Long> courseAreaIds, Set<Long> interestedAreaIds) {
        long matchCount = courseAreaIds.stream().filter(interestedAreaIds::contains).count();
        boolean majorRequired = MAJOR_REQUIRED.equals(course.getCourseType());
        int score = Math.toIntExact(matchCount) * INTEREST_MATCH_SCORE
                + (majorRequired ? MAJOR_REQUIRED_SCORE : 0);

        List<String> reasons = new ArrayList<>();
        if (matchCount > 0) {
            reasons.add("관심 분야 " + matchCount + "개 일치");
        }
        if (majorRequired) {
            reasons.add("전공필수 과목");
        }
        return new ScoreResult(score, List.copyOf(reasons));
    }

    public record ScoreResult(int score, List<String> reasons) {
    }
}
