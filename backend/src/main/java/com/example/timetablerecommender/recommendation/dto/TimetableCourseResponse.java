package com.example.timetablerecommender.recommendation.dto;

import java.util.List;

public record TimetableCourseResponse(
        String courseCode,
        String name,
        int credits,
        String sectionKey,
        List<MeetingTimeResponse> meetingTimes) {
}
