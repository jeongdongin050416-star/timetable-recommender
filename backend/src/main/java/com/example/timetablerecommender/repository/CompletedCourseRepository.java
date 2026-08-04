package com.example.timetablerecommender.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.timetablerecommender.domain.CompletedCourse;

public interface CompletedCourseRepository extends JpaRepository<CompletedCourse, Long> {

    @EntityGraph(attributePaths = "course")
    List<CompletedCourse> findByUserIdOrderByCourseCourseCodeAsc(Long userId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    long deleteByUserIdAndCourseId(Long userId, Long courseId);

    @Query("select completed.course.id from CompletedCourse completed where completed.user.id = :userId")
    Set<Long> findCourseIdsByUserId(Long userId);

    @Query("select completed.course.courseCode from CompletedCourse completed where completed.user.id = :userId")
    Set<String> findCourseCodesByUserId(Long userId);
}
