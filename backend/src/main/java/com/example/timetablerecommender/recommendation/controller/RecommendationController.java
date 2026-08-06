package com.example.timetablerecommender.recommendation.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.timetablerecommender.common.api.ApiResponse;
import com.example.timetablerecommender.auth.security.SessionUser;
import com.example.timetablerecommender.recommendation.dto.RecommendationResponse;
import com.example.timetablerecommender.recommendation.engine.StudentYear;
import com.example.timetablerecommender.recommendation.service.RecommendationService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/recommended-timetables")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    ApiResponse<RecommendationResponse> getRecommendedCourses(
            @AuthenticationPrincipal SessionUser user,
            @RequestParam @Min(value = 1, message = "1 이상이어야 합니다.")
            @Max(value = 20, message = "20 이하여야 합니다.") int targetCourseCount,
            @RequestParam StudentYear studentYear,
            @RequestParam(required = false) List<Long> interestedAreaIds,
            @RequestParam(required = false) List<Long> uninterestedAreaIds) {
        return ApiResponse.success(recommendationService.recommend(
                user.userId(), targetCourseCount, studentYear, interestedAreaIds, uninterestedAreaIds));
    }
}
