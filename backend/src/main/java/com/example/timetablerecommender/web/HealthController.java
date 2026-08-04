package com.example.timetablerecommender.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timetablerecommender.common.api.ApiResponse;

// @RestController는 이 클래스를 HTTP 요청을 처리하고 응답 본문을 반환하는 컨트롤러로 등록한다.
@RestController
// 클래스 안의 모든 API 경로 앞에 /api를 붙인다.
@RequestMapping("/api")
public class HealthController {

    // GET /api/health 요청이 들어올 때만 health()가 실행된다.
    @GetMapping("/health")
    ApiResponse<Map<String, String>> health() {
        // Java의 Map은 Spring에 의해 {"status":"ok"} 형태의 JSON 응답으로 변환된다.
        return ApiResponse.success(Map.of("status", "ok"));
    }
}
