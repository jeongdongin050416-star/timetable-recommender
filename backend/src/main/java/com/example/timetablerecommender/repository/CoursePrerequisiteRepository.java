package com.example.timetablerecommender.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.timetablerecommender.domain.CoursePrerequisite;
import com.example.timetablerecommender.domain.CoursePrerequisiteId;
import com.example.timetablerecommender.domain.RelationType;

public interface CoursePrerequisiteRepository
        extends JpaRepository<CoursePrerequisite, CoursePrerequisiteId> {
    @EntityGraph(attributePaths = {"course", "prerequisiteCourse"})
    List<CoursePrerequisite> findByCourseIdInAndRelationType(
            Collection<Long> courseIds, RelationType relationType);
}
