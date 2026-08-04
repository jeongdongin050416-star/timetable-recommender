package com.example.timetablerecommender.recommendation.engine;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.example.timetablerecommender.recommendation.conflict.MeetingTime;

public final class TimetableScorer {

    static final int INTERESTED_COURSE_SCORE = 30;
    static final int UNINTERESTED_COURSE_SCORE = -15;
    static final int MAJOR_REQUIRED_SCORE = 20;
    static final int RECOMMENDED_PREREQUISITE_MET_SCORE = 20;
    static final int RECOMMENDED_PREREQUISITE_UNMET_SCORE = -10;

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
            for (String prerequisite : course.recommendedPrerequisiteCodes()) {
                score += criteria.completedCourseCodes().contains(prerequisite)
                        ? RECOMMENDED_PREREQUISITE_MET_SCORE
                        : RECOMMENDED_PREREQUISITE_UNMET_SCORE;
            }
        }
        return score - gapPenalty(selections);
    }

    private int gapPenalty(List<TimetableSelection> selections) {
        Map<DayOfWeek, List<MeetingTime>> byDay = new EnumMap<>(DayOfWeek.class);
        for (TimetableSelection selection : selections) {
            for (MeetingTime meetingTime : selection.section().meetingTimes()) {
                byDay.computeIfAbsent(meetingTime.dayOfWeek(), ignored -> new ArrayList<>())
                        .add(meetingTime);
            }
        }

        int penalty = 0;
        for (List<MeetingTime> dailyTimes : byDay.values()) {
            dailyTimes.sort(Comparator.comparing(MeetingTime::startTime));
            for (int index = 1; index < dailyTimes.size(); index++) {
                long gapMinutes = Duration.between(
                        dailyTimes.get(index - 1).endTime(), dailyTimes.get(index).startTime()).toMinutes();
                penalty += Math.max(0, Math.toIntExact(gapMinutes / 30));
            }
        }
        return penalty;
    }
}
