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

import com.example.timetablerecommender.common.exception.ConflictingInterestAreaException;
import com.example.timetablerecommender.common.exception.InterestAreaNotFoundException;
import com.example.timetablerecommender.common.exception.UserNotFoundException;
import com.example.timetablerecommender.domain.Course;
import com.example.timetablerecommender.domain.CourseInterestArea;
import com.example.timetablerecommender.domain.CoursePrerequisite;
import com.example.timetablerecommender.domain.RelationType;
import com.example.timetablerecommender.recommendation.dto.InterestAreaResponse;
import com.example.timetablerecommender.recommendation.dto.RecommendationResponse;
import com.example.timetablerecommender.recommendation.dto.RecommendedCourseResponse;
import com.example.timetablerecommender.recommendation.policy.RecommendationPolicy;
import com.example.timetablerecommender.recommendation.policy.RecommendationPolicy.ScoreResult;
import com.example.timetablerecommender.repository.AppUserRepository;
import com.example.timetablerecommender.repository.CompletedCourseRepository;
import com.example.timetablerecommender.repository.CourseInterestAreaRepository;
import com.example.timetablerecommender.repository.CoursePrerequisiteRepository;
import com.example.timetablerecommender.repository.CourseRepository;
import com.example.timetablerecommender.repository.InterestAreaRepository;

@Service
public class RecommendationService {

    private final AppUserRepository userRepository;
    private final InterestAreaRepository interestAreaRepository;
    private final CourseRepository courseRepository;
    private final CompletedCourseRepository completedCourseRepository;
    private final CourseInterestAreaRepository courseInterestAreaRepository;
    private final CoursePrerequisiteRepository prerequisiteRepository;
    private final RecommendationPolicy recommendationPolicy;

    public RecommendationService(
            AppUserRepository userRepository,
            InterestAreaRepository interestAreaRepository,
            CourseRepository courseRepository,
            CompletedCourseRepository completedCourseRepository,
            CourseInterestAreaRepository courseInterestAreaRepository,
            CoursePrerequisiteRepository prerequisiteRepository,
            RecommendationPolicy recommendationPolicy) {
        this.userRepository = userRepository;
        this.interestAreaRepository = interestAreaRepository;
        this.courseRepository = courseRepository;
        this.completedCourseRepository = completedCourseRepository;
        this.courseInterestAreaRepository = courseInterestAreaRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.recommendationPolicy = recommendationPolicy;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse recommend(
            Long userId, int courseCount, List<Long> interestedIds, List<Long> excludedIds) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        Set<Long> interestedAreaIds = distinct(interestedIds);
        Set<Long> excludedAreaIds = distinct(excludedIds);
        Set<Long> overlap = new HashSet<>(interestedAreaIds);
        overlap.retainAll(excludedAreaIds);
        if (!overlap.isEmpty()) {
            throw new ConflictingInterestAreaException();
        }
        validateInterestAreas(interestedAreaIds, excludedAreaIds);

        List<Course> candidates = courseRepository.findRecommendationCandidates(userId);
        if (candidates.isEmpty()) {
            return new RecommendationResponse(userId, courseCount, 0, List.of());
        }

        List<Long> candidateIds = candidates.stream().map(Course::getId).toList();
        Map<Long, List<CourseInterestArea>> areasByCourse = loadAreas(candidateIds);
        Map<Long, Set<Long>> prerequisitesByCourse = loadPrerequisites(candidateIds);
        Set<Long> completedCourseIds = completedCourseRepository.findCourseIdsByUserId(userId);

        List<RecommendedCourseResponse> recommendations = new ArrayList<>();
        for (Course course : candidates) {
            List<CourseInterestArea> courseAreas = areasByCourse.getOrDefault(course.getId(), List.of());
            Set<Long> courseAreaIds = new HashSet<>();
            for (CourseInterestArea courseArea : courseAreas) {
                courseAreaIds.add(courseArea.getInterestArea().getId());
            }
            if (!java.util.Collections.disjoint(courseAreaIds, excludedAreaIds)) {
                continue;
            }
            if (!completedCourseIds.containsAll(
                    prerequisitesByCourse.getOrDefault(course.getId(), Set.of()))) {
                continue;
            }

            ScoreResult score = recommendationPolicy.score(course, courseAreaIds, interestedAreaIds);
            List<InterestAreaResponse> areaResponses = courseAreas.stream()
                    .map(link -> new InterestAreaResponse(
                            link.getInterestArea().getId(), link.getInterestArea().getName()))
                    .sorted(Comparator.comparing(InterestAreaResponse::interestAreaId))
                    .toList();
            recommendations.add(new RecommendedCourseResponse(
                    course.getCourseCode(),
                    course.getName(),
                    course.getCredits(),
                    score.score(),
                    areaResponses,
                    score.reasons()));
        }

        List<RecommendedCourseResponse> result = recommendations.stream()
                .sorted(Comparator.comparingInt(RecommendedCourseResponse::score).reversed()
                        .thenComparing(RecommendedCourseResponse::courseCode))
                .limit(courseCount)
                .toList();
        return new RecommendationResponse(userId, courseCount, result.size(), result);
    }

    private Set<Long> distinct(List<Long> ids) {
        return ids == null ? Set.of() : new LinkedHashSet<>(ids);
    }

    private void validateInterestAreas(Set<Long> interestedIds, Set<Long> excludedIds) {
        Set<Long> requested = new HashSet<>(interestedIds);
        requested.addAll(excludedIds);
        if (!requested.isEmpty() && interestAreaRepository.findAllById(requested).size() != requested.size()) {
            throw new InterestAreaNotFoundException();
        }
    }

    private Map<Long, List<CourseInterestArea>> loadAreas(List<Long> courseIds) {
        Map<Long, List<CourseInterestArea>> result = new HashMap<>();
        for (CourseInterestArea link : courseInterestAreaRepository.findByCourseIdIn(courseIds)) {
            result.computeIfAbsent(link.getCourse().getId(), ignored -> new ArrayList<>()).add(link);
        }
        return result;
    }

    private Map<Long, Set<Long>> loadPrerequisites(List<Long> courseIds) {
        Map<Long, Set<Long>> result = new HashMap<>();
        List<CoursePrerequisite> prerequisites = prerequisiteRepository
                .findByCourseIdInAndRelationType(courseIds, RelationType.PREREQUISITE);
        for (CoursePrerequisite prerequisite : prerequisites) {
            result.computeIfAbsent(prerequisite.getCourse().getId(), ignored -> new HashSet<>())
                    .add(prerequisite.getPrerequisiteCourse().getId());
        }
        return result;
    }
}
