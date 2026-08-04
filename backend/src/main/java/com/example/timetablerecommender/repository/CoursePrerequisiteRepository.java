package com.example.timetablerecommender.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.timetablerecommender.domain.CoursePrerequisite;
import com.example.timetablerecommender.domain.CoursePrerequisiteId;

public interface CoursePrerequisiteRepository
        extends JpaRepository<CoursePrerequisite, CoursePrerequisiteId> {
}
