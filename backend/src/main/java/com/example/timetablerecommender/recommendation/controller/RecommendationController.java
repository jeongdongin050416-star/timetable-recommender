package com.example.timetablerecommender.recommendation.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.timetablerecommender.common.api.ApiResponse;
import com.example.timetablerecommender.recommendation.dto.RecommendationResponse;
import com.example.timetablerecommender.recommendation.service.RecommendationService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/users/{userId}/recommended-courses")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    ApiResponse<RecommendationResponse> getRecommendedCourses(
            @PathVariable Long userId,
            @RequestParam @Min(value = 1, message = "1 이상이어야 합니다.")
            @Max(value = 20, message = "20 이하여야 합니다.") int courseCount,
            @RequestParam(required = false) List<Long> interestedAreaIds,
            @RequestParam(required = false) List<Long> excludedAreaIds) {
        return ApiResponse.success(recommendationService.recommend(
                userId, courseCount, interestedAreaIds, excludedAreaIds));
    }
}
