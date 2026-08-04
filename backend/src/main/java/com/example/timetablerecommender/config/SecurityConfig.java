package com.example.timetablerecommender.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// @Configuration은 이 클래스가 Spring 설정을 담고 있음을 나타낸다.
@Configuration
public class SecurityConfig {
    // @Bean을 붙이면 이 메서드가 반환한 SecurityFilterChain을 Spring 컨테이너가 관리한다.
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 현재는 초기 개발 단계이므로 CSRF 검사를 끄고 모든 요청을 허용한다.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .build();
    }
}
