package com.example.timetablerecommender.recommendation.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.example.timetablerecommender.recommendation.conflict.MeetingTime;
import com.example.timetablerecommender.recommendation.conflict.MeetingTimeConflictChecker;

public final class RecommendationEngine {

    private static final int MAX_RESULTS = 1;

    private final MeetingTimeConflictChecker conflictChecker;
    private final TimetableScorer scorer;

    public RecommendationEngine() {
        this(new MeetingTimeConflictChecker(), new TimetableScorer());
    }

    RecommendationEngine(MeetingTimeConflictChecker conflictChecker, TimetableScorer scorer) {
        this.conflictChecker = conflictChecker;
        this.scorer = scorer;
    }

    public List<RecommendedTimetable> recommend(
            java.util.Collection<CourseCandidate> inputCandidates,
            RecommendationCriteria criteria) {
        List<CourseCandidate> candidates = inputCandidates.stream()
                .filter(candidate -> isEligibleByCompletion(candidate, criteria))
                .sorted(Comparator.comparing(CourseCandidate::courseCode))
                .toList();
        List<RecommendedTimetable> results = new ArrayList<>();
        search(candidates, criteria, 0, new ArrayList<>(), new ArrayList<>(), results);

        return results.stream()
                .sorted(Comparator.comparingInt(RecommendedTimetable::score).reversed()
                        .thenComparing(this::courseCodeKey)
                        .thenComparing(this::sectionKey))
                .limit(MAX_RESULTS)
                .toList();
    }

    private boolean isEligibleByCompletion(CourseCandidate candidate, RecommendationCriteria criteria) {
        Set<String> completed = criteria.completedCourseCodes();
        return !("MAS110".equals(candidate.courseCode()) && completed.contains("MAS109"))
                && !("MAS109".equals(candidate.courseCode()) && completed.contains("MAS110"))
                && !("CS109".equals(candidate.courseCode()) && completed.contains("CS206"));
    }

    private void search(
            List<CourseCandidate> candidates,
            RecommendationCriteria criteria,
            int courseIndex,
            List<TimetableSelection> selected,
            List<MeetingTime> occupiedTimes,
            List<RecommendedTimetable> results) {
        if (selected.size() == criteria.targetCourseCount()) {
            results.add(new RecommendedTimetable(scorer.score(selected, criteria), selected));
            return;
        }
        int coursesNeeded = criteria.targetCourseCount() - selected.size();
        if (courseIndex >= candidates.size() || candidates.size() - courseIndex < coursesNeeded) {
            return;
        }

        CourseCandidate course = candidates.get(courseIndex);
        boolean courseAlreadySelected = selected.stream()
                .anyMatch(selection -> selection.course().courseCode().equals(course.courseCode()));
        boolean incompatibleCourseSelected = selected.stream()
                .anyMatch(selection -> course.incompatibleCourseCodes()
                        .contains(selection.course().courseCode()));
        List<SectionCandidate> sections = course.sections().stream()
                .sorted(Comparator.comparing(SectionCandidate::sectionKey))
                .toList();
        if (!courseAlreadySelected && !incompatibleCourseSelected) {
            for (SectionCandidate section : sections) {
                if (conflictChecker.conflicts(occupiedTimes, section.meetingTimes())) {
                    continue;
                }
                selected.add(new TimetableSelection(course, section));
                int previousTimeCount = occupiedTimes.size();
                occupiedTimes.addAll(section.meetingTimes());
                search(candidates, criteria, courseIndex + 1, selected, occupiedTimes, results);
                occupiedTimes.subList(previousTimeCount, occupiedTimes.size()).clear();
                selected.remove(selected.size() - 1);
            }
        }

        search(candidates, criteria, courseIndex + 1, selected, occupiedTimes, results);
    }

    private String courseCodeKey(RecommendedTimetable timetable) {
        return timetable.selections().stream()
                .map(selection -> selection.course().courseCode())
                .sorted()
                .collect(java.util.stream.Collectors.joining("\u0000"));
    }

    private String sectionKey(RecommendedTimetable timetable) {
        return timetable.selections().stream()
                .map(selection -> selection.section().sectionKey())
                .sorted()
                .collect(java.util.stream.Collectors.joining("\u0000"));
    }
}
