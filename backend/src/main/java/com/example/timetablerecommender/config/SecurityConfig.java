package com.example.timetablerecommender.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@Configuration: Spring 설절 클래스
public class SecurityConfig {

    @Bean//다음 method가 반환한 SecurituFilterChain 객체를 Spring이 관리하도록 등록함.
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .build();
    }//현재 backend로 들어오는 모든 요청을 로그인 여부와 관계없이 허용시킴. 따라서 현재는 누구나 API를 호출할 수 있음
}
