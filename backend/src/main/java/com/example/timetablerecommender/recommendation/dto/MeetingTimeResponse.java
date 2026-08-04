package com.example.timetablerecommender.recommendation.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record MeetingTimeResponse(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
}
