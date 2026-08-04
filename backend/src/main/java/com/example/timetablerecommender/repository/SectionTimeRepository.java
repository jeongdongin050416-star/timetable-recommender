package com.example.timetablerecommender.repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.timetablerecommender.domain.SectionTime;

public interface SectionTimeRepository extends JpaRepository<SectionTime, Long> {
    boolean existsBySectionIdAndDayOfWeekAndStartTimeAndEndTime(
            Long sectionId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);

    @EntityGraph(attributePaths = "section")
    List<SectionTime> findBySectionIdIn(Collection<Long> sectionIds);
}
