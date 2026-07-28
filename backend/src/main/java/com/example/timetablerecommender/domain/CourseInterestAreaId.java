package com.example.timetablerecommender.domain;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CourseInterestAreaId implements Serializable {

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "interest_area_id", nullable = false)
    private Long interestAreaId;

    protected CourseInterestAreaId() {
    }

    public CourseInterestAreaId(Long courseId, Long interestAreaId) {
        this.courseId = courseId;
        this.interestAreaId = interestAreaId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getInterestAreaId() {
        return interestAreaId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CourseInterestAreaId that)) {
            return false;
        }
        return Objects.equals(courseId, that.courseId)
                && Objects.equals(interestAreaId, that.interestAreaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, interestAreaId);
    }
}
