package com.example.timetablerecommender.course.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timetablerecommender.course.dto.CourseItemResponse;
import com.example.timetablerecommender.course.dto.CourseListResponse;
import com.example.timetablerecommender.repository.CourseRepository;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public CourseListResponse getCourses() {
        return new CourseListResponse(courseRepository.findAllByOrderByCourseCodeAsc()
                .stream()
                .map(course -> new CourseItemResponse(
                        course.getCourseCode(), course.getName(), course.getCredits(), course.getMainArea()))
                .toList());
    }
}
