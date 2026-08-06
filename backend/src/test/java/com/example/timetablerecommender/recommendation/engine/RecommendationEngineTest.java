package com.example.timetablerecommender.recommendation.engine;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.TUESDAY;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.timetablerecommender.recommendation.conflict.MeetingTime;
import com.example.timetablerecommender.recommendation.conflict.MeetingTimeConflictChecker;

class RecommendationEngineTest {

    private final RecommendationEngine engine = new RecommendationEngine();
    private final MeetingTimeConflictChecker conflictChecker = new MeetingTimeConflictChecker();

    @Test
    void selectsExactlyTargetCountOfDistinctCoursesAndOneSectionPerCourse() {
        List<CourseCandidate> candidates = List.of(
                course("CS100", false, Set.of(), Set.of(),
                        section("CS100-A", MONDAY, "09:00", "10:00"),
                        section("CS100-B", MONDAY, "10:00", "11:00")),
                course("CS200", false, Set.of(), Set.of(), section("CS200-A", TUESDAY, "09:00", "10:00")),
                course("CS300", false, Set.of(), Set.of(), section("CS300-A", TUESDAY, "10:00", "11:00")));

        List<RecommendedTimetable> results = engine.recommend(candidates, criteria(2));

        assertThat(results).isNotEmpty();
        for (RecommendedTimetable result : results) {
            assertThat(result.selections()).hasSize(2);
            assertThat(result.selections().stream().map(selection -> selection.course().courseCode()))
                    .doesNotHaveDuplicates();
        }
    }

    @Test
    void removesEveryCombinationContainingASectionTimeConflict() {
        List<CourseCandidate> candidates = List.of(
                course("CS100", false, Set.of(), Set.of(), section("CS100-A", MONDAY, "09:00", "11:00")),
                course("CS200", false, Set.of(), Set.of(), section("CS200-A", MONDAY, "10:00", "12:00")),
                course("CS300", false, Set.of(), Set.of(), section("CS300-A", TUESDAY, "10:00", "12:00")));

        List<RecommendedTimetable> results = engine.recommend(candidates, criteria(2));

        assertThat(results).hasSize(1);
        assertEveryResultIsConflictFree(results);
        assertThat(results).noneMatch(result -> courseCodes(result).equals(Set.of("CS100", "CS200")));
    }

    @Test
    void checksAllMeetingTimesOfASection() {
        SectionCandidate multipleTimes = new SectionCandidate("CS100-A", List.of(
                time(MONDAY, "09:00", "10:00"),
                time(TUESDAY, "14:00", "16:00")));
        List<CourseCandidate> candidates = List.of(
                course("CS100", false, Set.of(), Set.of(), multipleTimes),
                course("CS200", false, Set.of(), Set.of(), section("CS200-A", TUESDAY, "15:00", "17:00")));

        assertThat(engine.recommend(candidates, criteria(2))).isEmpty();
    }

    @Test
    void returnsOnlyTheHighestScoringCombination() {
        List<CourseCandidate> candidates = List.of(
                course("CS100", true, Set.of(), Set.of(), section("CS100-A", MONDAY, "09:00", "10:00")),
                course("CS200", false, Set.of(), Set.of(), section("CS200-A", TUESDAY, "09:00", "10:00")),
                course("CS300", false, Set.of(), Set.of(), section("CS300-A", MONDAY, "10:00", "11:00")),
                course("CS400", false, Set.of(), Set.of(), section("CS400-A", TUESDAY, "10:00", "11:00")));

        List<RecommendedTimetable> results = engine.recommend(candidates, criteria(2));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().selections().stream().map(s -> s.course().courseCode()))
                .contains("CS100");
    }

    @Test
    void timetableScorerAppliesEveryCourseScoringRule() {
        CourseCandidate first = course(
                "CS100", true, Set.of(1L), Set.of("PRE100", "PRE200"),
                section("CS100-A", MONDAY, "09:00", "10:00"));
        CourseCandidate second = course(
                "CS200", false, Set.of(2L), Set.of(),
                section("CS200-A", MONDAY, "11:00", "12:00"));
        RecommendationCriteria criteria = new RecommendationCriteria(
                2, Set.of(1L), Set.of(2L), Set.of("PRE100"));

        List<RecommendedTimetable> results = engine.recommend(List.of(first, second), criteria);

        // +30 interested, -15 uninterested, +20 major, +15 recommended met, +0 recommended unmet,
        // +15 first-year 100-level, +10 first-year 200-level.
        assertThat(results).singleElement().extracting(RecommendedTimetable::score).isEqualTo(75);
    }

    @Test
    void gapsBetweenClassesDoNotAffectScore() {
        CourseCandidate first = course(
                "CS100", false, Set.of(), Set.of(), section("CS100-A", MONDAY, "09:00", "10:00"));
        CourseCandidate second = course(
                "CS200", false, Set.of(), Set.of(), section("CS200-A", MONDAY, "12:00", "13:00"));
        CourseCandidate third = course(
                "CS300", false, Set.of(), Set.of(), section("CS300-A", TUESDAY, "18:00", "19:00"));

        assertThat(engine.recommend(List.of(first, second, third), criteria(3)))
                .singleElement().extracting(RecommendedTimetable::score).isEqualTo(25);
    }

    @Test
    void appliesCourseLevelScoresForEveryStudentYear() {
        assertThat(scoreFor("CS101", StudentYear.FIRST_YEAR)).isEqualTo(15);
        assertThat(scoreFor("CS201", StudentYear.FIRST_YEAR)).isEqualTo(10);
        assertThat(scoreFor("CS201", StudentYear.SECOND_YEAR)).isEqualTo(15);
        assertThat(scoreFor("CS301", StudentYear.SECOND_YEAR)).isEqualTo(10);
        assertThat(scoreFor("CS301", StudentYear.THIRD_YEAR)).isEqualTo(15);
        assertThat(scoreFor("CS201", StudentYear.THIRD_YEAR)).isEqualTo(10);
        assertThat(scoreFor("CS401", StudentYear.THIRD_YEAR)).isEqualTo(5);
        assertThat(scoreFor("CS301", StudentYear.FOURTH_YEAR_OR_ABOVE)).isEqualTo(14);
        assertThat(scoreFor("CS401", StudentYear.FOURTH_YEAR_OR_ABOVE)).isEqualTo(15);
    }

    @Test
    void scoresRecommendedAndRequiredPrerequisitesByCompletion() {
        CourseCandidate candidate = new CourseCandidate(
                "CS500", "선수과목 점수", 3, false, Set.of(),
                Set.of("REC_MET", "REC_UNMET"),
                Set.of("REQ_MET", "REQ_UNMET"),
                List.of(section("CS500-A", MONDAY, "09:00", "10:00")));
        RecommendationCriteria criteria = new RecommendationCriteria(
                1, StudentYear.FIRST_YEAR, Set.of(), Set.of(), Set.of("REC_MET", "REQ_MET"));

        // Recommended: +15 +0, prerequisite: +20 -20.
        assertThat(engine.recommend(List.of(candidate), criteria))
                .singleElement().extracting(RecommendedTimetable::score).isEqualTo(15);
    }

    @Test
    void givesNoStudentYearScoreToUnmatchedCourseLevelsOrInvalidCodes() {
        assertThat(scoreFor("CS301", StudentYear.FIRST_YEAR)).isZero();
        assertThat(scoreFor("CS500", StudentYear.FOURTH_YEAR_OR_ABOVE)).isZero();
        assertThat(scoreFor("SEMINAR", StudentYear.FIRST_YEAR)).isZero();
    }

    @Test
    void equalScoresAreSortedByCourseCodeThenSectionKey() {
        List<CourseCandidate> candidates = List.of(
                course("CS300", false, Set.of(), Set.of(), section("CS300-A", TUESDAY, "09:00", "10:00")),
                course("CS100", false, Set.of(), Set.of(),
                        section("CS100-B", MONDAY, "11:00", "12:00"),
                        section("CS100-A", MONDAY, "09:00", "10:00")),
                course("CS200", false, Set.of(), Set.of(), section("CS200-A", TUESDAY, "11:00", "12:00")));

        List<RecommendedTimetable> results = engine.recommend(candidates, criteria(1));

        assertThat(results).extracting(result -> result.selections().getFirst().course().courseCode())
                .containsExactly("CS100");
        assertThat(results.get(0).selections().getFirst().section().sectionKey()).isEqualTo("CS100-A");
    }

    @Test
    void doesNotModifyInputCollections() {
        CourseCandidate later = course(
                "CS200", false, Set.of(), Set.of(), section("CS200-A", TUESDAY, "09:00", "10:00"));
        CourseCandidate earlier = course(
                "CS100", false, Set.of(), Set.of(), section("CS100-A", MONDAY, "09:00", "10:00"));
        List<CourseCandidate> mutableInput = new ArrayList<>(List.of(later, earlier));
        List<CourseCandidate> originalOrder = List.copyOf(mutableInput);

        engine.recommend(mutableInput, criteria(1));

        assertThat(mutableInput).containsExactlyElementsOf(originalOrder);
    }

    @Test
    void returnsEmptyWhenExactTargetCountCannotBeBuilt() {
        CourseCandidate only = course(
                "CS100", false, Set.of(), Set.of(), section("CS100-A", MONDAY, "09:00", "10:00"));

        assertThat(engine.recommend(List.of(only), criteria(2))).isEmpty();
    }

    @Test
    void duplicateCourseCandidatesCanNeverFillTwoCourseSlots() {
        CourseCandidate firstSection = course(
                "CS100", false, Set.of(), Set.of(), section("CS100-A", MONDAY, "09:00", "10:00"));
        CourseCandidate secondSection = course(
                "CS100", false, Set.of(), Set.of(), section("CS100-B", TUESDAY, "09:00", "10:00"));

        assertThat(engine.recommend(List.of(firstSection, secondSection), criteria(2))).isEmpty();
    }

    private void assertEveryResultIsConflictFree(List<RecommendedTimetable> results) {
        for (RecommendedTimetable result : results) {
            List<TimetableSelection> selections = result.selections();
            for (int first = 0; first < selections.size(); first++) {
                for (int second = first + 1; second < selections.size(); second++) {
                    assertThat(conflictChecker.conflicts(
                            selections.get(first).section().meetingTimes(),
                            selections.get(second).section().meetingTimes())).isFalse();
                }
            }
        }
    }

    private Set<String> courseCodes(RecommendedTimetable result) {
        Set<String> codes = new HashSet<>();
        result.selections().forEach(selection -> codes.add(selection.course().courseCode()));
        return codes;
    }

    private static RecommendationCriteria criteria(int targetCourseCount) {
        return new RecommendationCriteria(targetCourseCount, Set.of(), Set.of(), Set.of());
    }

    private int scoreFor(String courseCode, StudentYear studentYear) {
        CourseCandidate candidate = course(
                courseCode, false, Set.of(), Set.of(),
                section(courseCode + "-A", MONDAY, "09:00", "10:00"));
        RecommendationCriteria criteria = new RecommendationCriteria(
                1, studentYear, Set.of(), Set.of(), Set.of());
        return engine.recommend(List.of(candidate), criteria).getFirst().score();
    }

    private static CourseCandidate course(
            String code,
            boolean majorRequired,
            Set<Long> areas,
            Set<String> recommendedPrerequisites,
            SectionCandidate... sections) {
        return new CourseCandidate(
                code, code + " 과목", 3, majorRequired, areas, recommendedPrerequisites, List.of(sections));
    }

    private static SectionCandidate section(
            String key, java.time.DayOfWeek day, String start, String end) {
        return new SectionCandidate(key, List.of(time(day, start, end)));
    }

    private static MeetingTime time(java.time.DayOfWeek day, String start, String end) {
        return new MeetingTime(day, LocalTime.parse(start), LocalTime.parse(end));
    }
}
