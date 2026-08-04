package com.example.timetablerecommender.recommendation.conflict;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * 수업 시간 충돌 계산에 필요한 값만 담는 순수 Java 값 객체이다.
 */
public record MeetingTime(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {

    public MeetingTime {
        Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
        Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(endTime, "endTime must not be null");
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }
}
