package com.example.timetablerecommender.course.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timetablerecommender.common.api.ApiResponse;
import com.example.timetablerecommender.course.dto.CourseListResponse;
import com.example.timetablerecommender.course.service.CourseService;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    ApiResponse<CourseListResponse> getCourses() {
        return ApiResponse.success(courseService.getCourses());
    }
}
