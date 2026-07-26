package com.example.timetablerecommender.domain;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CoursePrerequisiteId implements Serializable {

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "prerequisite_course_id", nullable = false)
    private Long prerequisiteCourseId;

    protected CoursePrerequisiteId() {
    }

    public CoursePrerequisiteId(Long courseId, Long prerequisiteCourseId) {
        this.courseId = courseId;
        this.prerequisiteCourseId = prerequisiteCourseId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getPrerequisiteCourseId() {
        return prerequisiteCourseId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CoursePrerequisiteId that)) {
            return false;
        }
        return Objects.equals(courseId, that.courseId)
                && Objects.equals(prerequisiteCourseId, that.prerequisiteCourseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, prerequisiteCourseId);
    }
}
