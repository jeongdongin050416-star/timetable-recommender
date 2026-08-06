package com.example.timetablerecommender.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "course",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_course_code",
                columnNames = "course_code"
        )
)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_code", nullable = false, length = 30)
    private String courseCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "credits", nullable = false)
    private Integer credits;

    @Column(name = "course_type", nullable = false, length = 50)
    private String courseType;

    @Column(name = "main_area", length = 100)
    private String mainArea;

    protected Course() {
    }

    public Course(String courseCode, String name, Integer credits, String courseType) {
        this.courseCode = courseCode;
        this.name = name;
        this.credits = credits;
        this.courseType = courseType;
    }

    public Long getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getName() {
        return name;
    }

    public Integer getCredits() {
        return credits;
    }

    public String getCourseType() {
        return courseType;
    }

    public String getMainArea() {
        return mainArea;
    }

    public void assignMainArea(String mainArea) {
        this.mainArea = mainArea;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Course course)) {
            return false;
        }
        return id != null && Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
