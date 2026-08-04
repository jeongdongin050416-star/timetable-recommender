package com.example.timetablerecommender.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.timetablerecommender.domain.CourseInterestArea;
import com.example.timetablerecommender.domain.CourseInterestAreaId;

public interface CourseInterestAreaRepository extends JpaRepository<CourseInterestArea, CourseInterestAreaId> {
    @EntityGraph(attributePaths = {"course", "interestArea"})
    List<CourseInterestArea> findByCourseIdIn(Collection<Long> courseIds);
}
