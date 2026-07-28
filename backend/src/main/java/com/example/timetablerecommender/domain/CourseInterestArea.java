package com.example.timetablerecommender.domain;

import java.util.Objects;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_interest_area")
public class CourseInterestArea {

    @EmbeddedId
    private CourseInterestAreaId id;

    @MapsId("courseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @MapsId("interestAreaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interest_area_id", nullable = false)
    private InterestArea interestArea;

    protected CourseInterestArea() {
    }

    public CourseInterestArea(Course course, InterestArea interestArea) {
        this.course = course;
        this.interestArea = interestArea;
        this.id = new CourseInterestAreaId(course.getId(), interestArea.getId());
    }

    public CourseInterestAreaId getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public InterestArea getInterestArea() {
        return interestArea;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CourseInterestArea that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
