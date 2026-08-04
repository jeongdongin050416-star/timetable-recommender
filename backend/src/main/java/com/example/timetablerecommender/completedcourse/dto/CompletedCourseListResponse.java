package com.example.timetablerecommender.completedcourse.dto;

import java.util.List;

public record CompletedCourseListResponse(Long userId, List<CompletedCourseItemResponse> courses) {
}
