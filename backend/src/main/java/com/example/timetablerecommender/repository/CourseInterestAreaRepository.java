package com.example.timetablerecommender.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.timetablerecommender.domain.CourseInterestArea;
import com.example.timetablerecommender.domain.CourseInterestAreaId;

public interface CourseInterestAreaRepository extends JpaRepository<CourseInterestArea, CourseInterestAreaId> {
}
