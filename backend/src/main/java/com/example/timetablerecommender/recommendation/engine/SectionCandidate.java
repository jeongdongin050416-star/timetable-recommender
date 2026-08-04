package com.example.timetablerecommender.recommendation.engine;

import java.util.List;

import com.example.timetablerecommender.recommendation.conflict.MeetingTime;

public record SectionCandidate(String sectionKey, List<MeetingTime> meetingTimes) {

    public SectionCandidate {
        if (sectionKey == null || sectionKey.isBlank()) {
            throw new IllegalArgumentException("sectionKey must not be blank");
        }
        meetingTimes = List.copyOf(meetingTimes);
    }
}
