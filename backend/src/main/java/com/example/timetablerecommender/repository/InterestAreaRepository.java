package com.example.timetablerecommender.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.timetablerecommender.domain.InterestArea;

public interface InterestAreaRepository extends JpaRepository<InterestArea, Long> {
    Optional<InterestArea> findByName(String name);

    boolean existsByName(String name);
}
