package com.example.timetablerecommender.recommendation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timetablerecommender.common.exception.InterestAreaNotFoundException;
import com.example.timetablerecommender.common.exception.UserNotFoundException;
import com.example.timetablerecommender.domain.Course;
import com.example.timetablerecommender.domain.CourseInterestArea;
import com.example.timetablerecommender.domain.CoursePrerequisite;
import com.example.timetablerecommender.domain.CourseSection;
import com.example.timetablerecommender.domain.RelationType;
import com.example.timetablerecommender.domain.SectionTime;
import com.example.timetablerecommender.recommendation.conflict.MeetingTime;
import com.example.timetablerecommender.recommendation.dto.MeetingTimeResponse;
import com.example.timetablerecommender.recommendation.dto.RecommendationResponse;
import com.example.timetablerecommender.recommendation.dto.TimetableCourseResponse;
import com.example.timetablerecommender.recommendation.dto.TimetableResponse;
import com.example.timetablerecommender.recommendation.engine.CourseCandidate;
import com.example.timetablerecommender.recommendation.engine.RecommendationCriteria;
import com.example.timetablerecommender.recommendation.engine.RecommendationEngine;
import com.example.timetablerecommender.recommendation.engine.RecommendedTimetable;
import com.example.timetablerecommender.recommendation.engine.SectionCandidate;
import com.example.timetablerecommender.repository.AppUserRepository;
import com.example.timetablerecommender.repository.CompletedCourseRepository;
import com.example.timetablerecommender.repository.CourseInterestAreaRepository;
import com.example.timetablerecommender.repository.CoursePrerequisiteRepository;
import com.example.timetablerecommender.repository.CourseRepository;
import com.example.timetablerecommender.repository.CourseSectionRepository;
import com.example.timetablerecommender.repository.InterestAreaRepository;
import com.example.timetablerecommender.repository.SectionTimeRepository;

@Service
public class RecommendationService {

    private static final String MAJOR_REQUIRED = "MAJOR_REQUIRED";

    private final AppUserRepository userRepository;
    private final InterestAreaRepository interestAreaRepository;
    private final CourseRepository courseRepository;
    private final CompletedCourseRepository completedCourseRepository;
    private final CourseInterestAreaRepository courseInterestAreaRepository;
    private final CoursePrerequisiteRepository prerequisiteRepository;
    private final CourseSectionRepository sectionRepository;
    private final SectionTimeRepository sectionTimeRepository;
    private final RecommendationEngine recommendationEngine = new RecommendationEngine();

    public RecommendationService(
            AppUserRepository userRepository,
            InterestAreaRepository interestAreaRepository,
            CourseRepository courseRepository,
            CompletedCourseRepository completedCourseRepository,
            CourseInterestAreaRepository courseInterestAreaRepository,
            CoursePrerequisiteRepository prerequisiteRepository,
            CourseSectionRepository sectionRepository,
            SectionTimeRepository sectionTimeRepository) {
        this.userRepository = userRepository;
        this.interestAreaRepository = interestAreaRepository;
        this.courseRepository = courseRepository;
        this.completedCourseRepository = completedCourseRepository;
        this.courseInterestAreaRepository = courseInterestAreaRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.sectionRepository = sectionRepository;
        this.sectionTimeRepository = sectionTimeRepository;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse recommend(
            Long userId,
            int targetCourseCount,
            List<Long> interestedIds,
            List<Long> uninterestedIds) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        Set<Long> interestedAreaIds = distinct(interestedIds);
        Set<Long> uninterestedAreaIds = distinct(uninterestedIds);
        validateInterestAreas(interestedAreaIds, uninterestedAreaIds);

        List<Course> courses = courseRepository.findRecommendationCandidates(userId);
        if (courses.isEmpty()) {
            return new RecommendationResponse(userId, targetCourseCount, null);
        }

        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Map<Long, Set<Long>> areasByCourse = loadAreas(courseIds);
        Map<Long, Set<String>> recommendedPrerequisites = loadRecommendedPrerequisites(courseIds);
        Map<Long, List<SectionCandidate>> sectionsByCourse = loadSections(courseIds);

        List<CourseCandidate> candidates = courses.stream()
                .map(course -> new CourseCandidate(
                        course.getCourseCode(),
                        course.getName(),
                        course.getCredits(),
                        MAJOR_REQUIRED.equals(course.getCourseType()),
                        areasByCourse.getOrDefault(course.getId(), Set.of()),
                        recommendedPrerequisites.getOrDefault(course.getId(), Set.of()),
                        sectionsByCourse.getOrDefault(course.getId(), List.of())))
                .toList();
        RecommendationCriteria criteria = new RecommendationCriteria(
                targetCourseCount,
                interestedAreaIds,
                uninterestedAreaIds,
                completedCourseRepository.findCourseCodesByUserId(userId));
        TimetableResponse timetable = recommendationEngine.recommend(candidates, criteria).stream()
                .findFirst()
                .map(this::toResponse)
                .orElse(null);
        return new RecommendationResponse(userId, targetCourseCount, timetable);
    }

    private Set<Long> distinct(List<Long> ids) {
        return ids == null ? Set.of() : new LinkedHashSet<>(ids);
    }

    private void validateInterestAreas(Set<Long> interestedIds, Set<Long> uninterestedIds) {
        Set<Long> requested = new HashSet<>(interestedIds);
        requested.addAll(uninterestedIds);
        if (!requested.isEmpty() && interestAreaRepository.findAllById(requested).size() != requested.size()) {
            throw new InterestAreaNotFoundException();
        }
    }

    private Map<Long, Set<Long>> loadAreas(List<Long> courseIds) {
        Map<Long, Set<Long>> result = new HashMap<>();
        for (CourseInterestArea link : courseInterestAreaRepository.findByCourseIdIn(courseIds)) {
            result.computeIfAbsent(link.getCourse().getId(), ignored -> new HashSet<>())
                    .add(link.getInterestArea().getId());
        }
        return result;
    }

    private Map<Long, Set<String>> loadRecommendedPrerequisites(List<Long> courseIds) {
        Map<Long, Set<String>> result = new HashMap<>();
        List<CoursePrerequisite> prerequisites = prerequisiteRepository
                .findByCourseIdInAndRelationType(courseIds, RelationType.RECOMMENDED);
        for (CoursePrerequisite prerequisite : prerequisites) {
            result.computeIfAbsent(prerequisite.getCourse().getId(), ignored -> new HashSet<>())
                    .add(prerequisite.getPrerequisiteCourse().getCourseCode());
        }
        return result;
    }

    private Map<Long, List<SectionCandidate>> loadSections(List<Long> courseIds) {
        List<CourseSection> sections = sectionRepository
                .findByCourseIdInOrderByCourseCourseCodeAscSectionNumberAsc(courseIds);
        if (sections.isEmpty()) {
            return Map.of();
        }
        List<Long> sectionIds = sections.stream().map(CourseSection::getId).toList();
        Map<Long, List<MeetingTime>> timesBySection = new HashMap<>();
        for (SectionTime time : sectionTimeRepository.findBySectionIdIn(sectionIds)) {
            timesBySection.computeIfAbsent(time.getSection().getId(), ignored -> new ArrayList<>())
                    .add(new MeetingTime(time.getDayOfWeek(), time.getStartTime(), time.getEndTime()));
        }

        Map<Long, List<SectionCandidate>> result = new HashMap<>();
        for (CourseSection section : sections) {
            List<MeetingTime> meetingTimes = timesBySection.getOrDefault(section.getId(), List.of())
                    .stream()
                    .sorted(Comparator.comparing(MeetingTime::dayOfWeek)
                            .thenComparing(MeetingTime::startTime))
                    .toList();
            String key = String.join("-",
                    section.getCourse().getCourseCode(),
                    section.getYear().toString(),
                    section.getSemester(),
                    section.getSectionNumber());
            result.computeIfAbsent(section.getCourse().getId(), ignored -> new ArrayList<>())
                    .add(new SectionCandidate(key, meetingTimes));
        }
        return result;
    }

    private TimetableResponse toResponse(RecommendedTimetable timetable) {
        List<TimetableCourseResponse> courses = timetable.selections().stream()
                .map(selection -> new TimetableCourseResponse(
                        selection.course().courseCode(),
                        selection.course().name(),
                        selection.course().credits(),
                        selection.section().sectionKey(),
                        selection.section().meetingTimes().stream()
                                .map(time -> new MeetingTimeResponse(
                                        time.dayOfWeek(), time.startTime(), time.endTime()))
                                .toList()))
                .toList();
        return new TimetableResponse(timetable.score(), courses.size(), courses);
    }
}
