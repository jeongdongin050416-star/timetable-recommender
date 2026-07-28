package com.example.timetablerecommender.domain;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "completed_course",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_completed_course_user_course",
                columnNames = {"user_id", "course_id"}
        )
)
public class CompletedCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    protected CompletedCourse() {
    }

    public CompletedCourse(AppUser user, Course course) {
        this.user = user;
        this.course = course;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Course getCourse() {
        return course;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CompletedCourse completedCourse)) {
            return false;
        }
        return id != null && Objects.equals(id, completedCourse.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
