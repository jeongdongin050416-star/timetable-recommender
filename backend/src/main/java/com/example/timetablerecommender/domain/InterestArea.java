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
        name = "interest_area",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_interest_area_name",
                columnNames = "name"
        )
)
public class InterestArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    protected InterestArea() {
    }

    public InterestArea(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof InterestArea interestArea)) {
            return false;
        }
        return id != null && Objects.equals(id, interestArea.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
