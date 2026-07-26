package com.example.timetablerecommender.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_prerequisite")
public class CoursePrerequisite {

    @EmbeddedId
    private CoursePrerequisiteId id;

    @MapsId("courseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @MapsId("prerequisiteCourseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prerequisite_course_id", nullable = false)
    private Course prerequisiteCourse;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 20)
    private RelationType relationType;

    protected CoursePrerequisite() {
    }

    public CoursePrerequisite(
            Course course,
            Course prerequisiteCourse,
            RelationType relationType
    ) {
        this.course = course;
        this.prerequisiteCourse = prerequisiteCourse;
        this.relationType = relationType;
        this.id = new CoursePrerequisiteId(course.getId(), prerequisiteCourse.getId());
    }

    public CoursePrerequisiteId getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public Course getPrerequisiteCourse() {
        return prerequisiteCourse;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CoursePrerequisite that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
