package com.example.timetablerecommender.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.timetablerecommender.domain.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
}
