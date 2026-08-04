package com.example.timetablerecommender.completedcourse.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timetablerecommender.common.api.ApiResponse;
import com.example.timetablerecommender.completedcourse.dto.CompletedCourseListResponse;
import com.example.timetablerecommender.completedcourse.dto.CompletedCourseStatusResponse;
import com.example.timetablerecommender.completedcourse.service.CompletedCourseService;

@RestController
@RequestMapping("/api/users/{userId}/completed-courses")
public class CompletedCourseController {

    private final CompletedCourseService completedCourseService;

    public CompletedCourseController(CompletedCourseService completedCourseService) {
        this.completedCourseService = completedCourseService;
    }

    @GetMapping
    ApiResponse<CompletedCourseListResponse> getCompletedCourses(@PathVariable Long userId) {
        return ApiResponse.success(completedCourseService.getCompletedCourses(userId));
    }

    @PutMapping("/{courseCode}")
    ApiResponse<CompletedCourseStatusResponse> addCompletedCourse(
            @PathVariable Long userId, @PathVariable String courseCode) {
        return ApiResponse.success(completedCourseService.addCompletedCourse(userId, courseCode));
    }

    @DeleteMapping("/{courseCode}")
    ApiResponse<CompletedCourseStatusResponse> deleteCompletedCourse(
            @PathVariable Long userId, @PathVariable String courseCode) {
        return ApiResponse.success(completedCourseService.deleteCompletedCourse(userId, courseCode));
    }
}
