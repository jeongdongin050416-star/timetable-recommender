package com.example.timetablerecommender.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.timetablerecommender.domain.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}
