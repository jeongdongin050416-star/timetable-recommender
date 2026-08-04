package com.example.timetablerecommender.completedcourse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.timetablerecommender.domain.AppUser;
import com.example.timetablerecommender.domain.CompletedCourse;
import com.example.timetablerecommender.domain.Course;
import com.example.timetablerecommender.repository.CompletedCourseRepository;

@Service
class CompletedCourseWriter {

    private final CompletedCourseRepository completedCourseRepository;

    CompletedCourseWriter(CompletedCourseRepository completedCourseRepository) {
        this.completedCourseRepository = completedCourseRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void insert(AppUser user, Course course) {
        completedCourseRepository.saveAndFlush(new CompletedCourse(user, course));
    }
}
