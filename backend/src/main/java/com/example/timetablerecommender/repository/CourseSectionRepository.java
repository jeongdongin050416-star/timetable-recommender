package com.example.timetablerecommender.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.timetablerecommender.domain.CourseSection;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
    Optional<CourseSection> findByCourseCourseCodeAndYearAndSemesterAndSectionNumber(
            String courseCode, Integer year, String semester, String sectionNumber);
}
