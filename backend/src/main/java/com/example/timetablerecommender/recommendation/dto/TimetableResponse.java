package com.example.timetablerecommender.recommendation.dto;

import java.util.List;

public record TimetableResponse(int score, int courseCount, List<TimetableCourseResponse> courses) {
}
