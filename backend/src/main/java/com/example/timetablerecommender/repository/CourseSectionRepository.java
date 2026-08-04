package com.example.timetablerecommender.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.timetablerecommender.domain.CourseSection;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
    Optional<CourseSection> findByCourseCourseCodeAndYearAndSemesterAndSectionNumber(
            String courseCode, Integer year, String semester, String sectionNumber);

    @EntityGraph(attributePaths = "course")
    List<CourseSection> findByCourseIdInOrderByCourseCourseCodeAscSectionNumberAsc(
            Collection<Long> courseIds);
}
