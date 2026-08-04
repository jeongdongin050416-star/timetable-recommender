package com.example.timetablerecommender.recommendation.conflict;

import java.util.Collection;
import java.util.Objects;

/**
 * Spring 및 JPA에 의존하지 않는 수업 시간 충돌 검사기이다.
 */
public final class MeetingTimeConflictChecker {

    /**
     * 두 개의 시간 구간이 겹치는지 검사한다. 끝 시각과 시작 시각이 같은 구간은 겹치지 않는다.
     */
    public boolean conflicts(MeetingTime first, MeetingTime second) {
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");

        return first.dayOfWeek() == second.dayOfWeek()
                && first.startTime().isBefore(second.endTime())
                && second.startTime().isBefore(first.endTime());
    }

    /**
     * 여러 시간대를 가질 수 있는 두 section 사이에 하나라도 충돌이 있는지 검사한다.
     */
    public boolean conflicts(
            Collection<MeetingTime> firstSectionTimes,
            Collection<MeetingTime> secondSectionTimes) {
        Objects.requireNonNull(firstSectionTimes, "firstSectionTimes must not be null");
        Objects.requireNonNull(secondSectionTimes, "secondSectionTimes must not be null");

        for (MeetingTime first : firstSectionTimes) {
            for (MeetingTime second : secondSectionTimes) {
                if (conflicts(first, second)) {
                    return true;
                }
            }
        }
        return false;
    }
}
