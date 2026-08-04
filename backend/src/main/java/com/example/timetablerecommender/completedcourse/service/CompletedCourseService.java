package com.example.timetablerecommender.completedcourse.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timetablerecommender.common.exception.CourseNotFoundException;
import com.example.timetablerecommender.common.exception.UserNotFoundException;
import com.example.timetablerecommender.completedcourse.dto.CompletedCourseItemResponse;
import com.example.timetablerecommender.completedcourse.dto.CompletedCourseListResponse;
import com.example.timetablerecommender.completedcourse.dto.CompletedCourseStatusResponse;
import com.example.timetablerecommender.domain.AppUser;
import com.example.timetablerecommender.domain.Course;
import com.example.timetablerecommender.repository.AppUserRepository;
import com.example.timetablerecommender.repository.CompletedCourseRepository;
import com.example.timetablerecommender.repository.CourseRepository;

@Service
public class CompletedCourseService {

    private final AppUserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CompletedCourseRepository completedCourseRepository;
    private final CompletedCourseWriter completedCourseWriter;

    public CompletedCourseService(
            AppUserRepository userRepository,
            CourseRepository courseRepository,
            CompletedCourseRepository completedCourseRepository,
            CompletedCourseWriter completedCourseWriter) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.completedCourseRepository = completedCourseRepository;
        this.completedCourseWriter = completedCourseWriter;
    }

    @Transactional(readOnly = true)
    public CompletedCourseListResponse getCompletedCourses(Long userId) {
        requireUser(userId);
        List<CompletedCourseItemResponse> courses = completedCourseRepository
                .findByUserIdOrderByCourseCourseCodeAsc(userId)
                .stream()
                .map(completed -> {
                    Course course = completed.getCourse();
                    return new CompletedCourseItemResponse(
                            course.getId(), course.getCourseCode(), course.getName(), course.getCredits());
                })
                .toList();
        return new CompletedCourseListResponse(userId, courses);
    }

    @Transactional
    public CompletedCourseStatusResponse addCompletedCourse(Long userId, Long courseId) {
        AppUser user = requireUser(userId);
        Course course = requireCourse(courseId);
        if (!completedCourseRepository.existsByUserIdAndCourseId(userId, courseId)) {
            try {
                completedCourseWriter.insert(user, course);
            } catch (DataIntegrityViolationException ignored) {
                // The database unique constraint makes a concurrent identical PUT idempotent.
            }
        }
        return new CompletedCourseStatusResponse(userId, courseId, true);
    }

    @Transactional
    public CompletedCourseStatusResponse deleteCompletedCourse(Long userId, Long courseId) {
        requireUser(userId);
        requireCourse(courseId);
        completedCourseRepository.deleteByUserIdAndCourseId(userId, courseId);
        return new CompletedCourseStatusResponse(userId, courseId, false);
    }

    private AppUser requireUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    private Course requireCourse(Long courseId) {
        return courseRepository.findById(courseId).orElseThrow(CourseNotFoundException::new);
    }
}
