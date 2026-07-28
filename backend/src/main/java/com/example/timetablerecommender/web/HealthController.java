package com.example.timetablerecommender.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// 이 파일은 HTTP 요청을 받아 응답하는 컨트롤러
@RestController
//@RestController 이 클래스를 HTTP요청을 처리하는 REST 컨트롤러로 등록함.
@RequestMapping("/api")
//@RequestMapping("/api")의 뜻: 이 클래스의 모든 API 주소 앞에 /api를 붙인다.
public class HealthController {

    @GetMapping("/health")//GET 방식의 /health요청을 health()메소드와 연결함
    Map<String, String> health() {
        return Map.of("status", "ok");
    }//health() method는 Java의 Map 객체를 반환함.
}
