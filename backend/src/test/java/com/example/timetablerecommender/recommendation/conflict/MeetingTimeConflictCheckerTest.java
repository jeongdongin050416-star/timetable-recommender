package com.example.timetablerecommender.recommendation.conflict;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.TUESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MeetingTimeConflictCheckerTest {

    private final MeetingTimeConflictChecker checker = new MeetingTimeConflictChecker();

    @ParameterizedTest(name = "{0}")
    @MethodSource("conflictingRanges")
    void detectsOverlappingRanges(String description, MeetingTime first, MeetingTime second) {
        assertThat(checker.conflicts(first, second)).isTrue();
        assertThat(checker.conflicts(second, first)).isTrue();
    }

    static List<Arguments> conflictingRanges() {
        return List.of(
                Arguments.of("부분적으로 겹침", time(MONDAY, "09:00", "10:00"), time(MONDAY, "09:30", "10:30")),
                Arguments.of("완전히 같은 구간", time(MONDAY, "09:00", "10:00"), time(MONDAY, "09:00", "10:00")),
                Arguments.of("한 구간이 다른 구간을 포함", time(MONDAY, "09:00", "12:00"), time(MONDAY, "10:00", "11:00")),
                Arguments.of("시작 시각만 같음", time(MONDAY, "09:00", "10:00"), time(MONDAY, "09:00", "09:30")),
                Arguments.of("종료 시각만 같음", time(MONDAY, "09:00", "10:00"), time(MONDAY, "09:30", "10:00")),
                Arguments.of(
                        "1나노초만 겹침",
                        new MeetingTime(MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                        new MeetingTime(MONDAY, LocalTime.of(9, 59, 59, 999_999_999), LocalTime.of(11, 0))),
                Arguments.of(
                        "하루 시작 경계에서 겹침",
                        new MeetingTime(MONDAY, LocalTime.MIN, LocalTime.of(0, 0, 0, 2)),
                        new MeetingTime(MONDAY, LocalTime.of(0, 0, 0, 1), LocalTime.of(0, 0, 0, 3))),
                Arguments.of(
                        "하루 끝 경계에서 겹침",
                        new MeetingTime(MONDAY, LocalTime.of(23, 59, 59, 999_999_997), LocalTime.MAX),
                        new MeetingTime(
                                MONDAY,
                                LocalTime.of(23, 59, 59, 999_999_998),
                                LocalTime.of(23, 59, 59, 999_999_999))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nonConflictingRanges")
    void acceptsNonOverlappingRanges(String description, MeetingTime first, MeetingTime second) {
        assertThat(checker.conflicts(first, second)).isFalse();
        assertThat(checker.conflicts(second, first)).isFalse();
    }

    static List<Arguments> nonConflictingRanges() {
        return List.of(
                Arguments.of("요일이 다름", time(MONDAY, "09:00", "11:00"), time(TUESDAY, "09:00", "11:00")),
                Arguments.of("첫 수업 종료와 둘째 수업 시작이 같음", time(MONDAY, "09:00", "10:00"), time(MONDAY, "10:00", "11:00")),
                Arguments.of("둘째 수업 종료와 첫 수업 시작이 같음", time(MONDAY, "10:00", "11:00"), time(MONDAY, "09:00", "10:00")),
                Arguments.of("구간 사이에 간격이 있음", time(MONDAY, "09:00", "10:00"), time(MONDAY, "10:01", "11:00")),
                Arguments.of(
                        "1나노초 간격",
                        new MeetingTime(MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                        new MeetingTime(MONDAY, LocalTime.of(10, 0, 0, 1), LocalTime.of(11, 0))));
    }

    @Test
    void detectsConflictAcrossMultipleSectionTimes() {
        List<MeetingTime> firstSection = List.of(
                time(MONDAY, "09:00", "10:00"),
                time(TUESDAY, "13:00", "15:00"));
        List<MeetingTime> secondSection = List.of(
                time(MONDAY, "10:00", "11:00"),
                time(TUESDAY, "14:59", "16:00"));

        assertThat(checker.conflicts(firstSection, secondSection)).isTrue();
    }

    @Test
    void acceptsMultipleSectionTimesWhenNoPairConflicts() {
        List<MeetingTime> firstSection = List.of(
                time(MONDAY, "09:00", "10:00"),
                time(TUESDAY, "13:00", "15:00"));
        List<MeetingTime> secondSection = List.of(
                time(MONDAY, "10:00", "11:00"),
                time(TUESDAY, "15:00", "16:00"));

        assertThat(checker.conflicts(firstSection, secondSection)).isFalse();
    }

    @Test
    void emptySectionHasNoConflict() {
        assertThat(checker.conflicts(List.of(), List.of(time(MONDAY, "09:00", "10:00")))).isFalse();
        assertThat(checker.conflicts(List.of(time(MONDAY, "09:00", "10:00")), List.of())).isFalse();
        assertThat(checker.conflicts(List.of(), List.of())).isFalse();
    }

    @Test
    void meetingTimeRejectsZeroLengthAndReversedRanges() {
        assertThatThrownBy(() -> time(MONDAY, "10:00", "10:00"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> time(MONDAY, "11:00", "10:00"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static MeetingTime time(java.time.DayOfWeek day, String start, String end) {
        return new MeetingTime(day, LocalTime.parse(start), LocalTime.parse(end));
    }
}
