package com.example.timetablerecommender.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.timetablerecommender.domain.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);

    @Query("""
            select course
            from Course course
            where not exists (
                select completed.id
                from CompletedCourse completed
                where completed.user.id = :userId
                  and completed.course.id = course.id
            )
            order by course.courseCode asc
            """)
    List<Course> findRecommendationCandidates(@Param("userId") Long userId);
}
