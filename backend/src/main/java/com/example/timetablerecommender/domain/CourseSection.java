package com.example.timetablerecommender.domain;

import java.util.Objects;

import jakarta.persistence.Column;
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
        name = "course_section",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_section_course_term_number",
                columnNames = {"course_id", "year", "semester", "section_number"}
        )
)
public class CourseSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "semester", nullable = false, length = 30)
    private String semester;

    @Column(name = "section_number", nullable = false, length = 30)
    private String sectionNumber;

    protected CourseSection() {
    }

    public CourseSection(Course course, Integer year, String semester, String sectionNumber) {
        this.course = course;
        this.year = year;
        this.semester = semester;
        this.sectionNumber = sectionNumber;
    }

    public Long getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public Integer getYear() {
        return year;
    }

    public String getSemester() {
        return semester;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CourseSection courseSection)) {
            return false;
        }
        return id != null && Objects.equals(id, courseSection.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
