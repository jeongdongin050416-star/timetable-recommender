package com.example.timetablerecommender.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// @Configuration은 이 클래스가 Spring 설정을 담고 있음을 나타낸다.
@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // @Bean을 붙이면 이 메서드가 반환한 SecurityFilterChain을 Spring 컨테이너가 관리한다.
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/health").permitAll()
                        .requestMatchers("/api/**").hasRole("USER")
                        .anyRequest().permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> writeSecurityError(
                                response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."))
                        .accessDeniedHandler((request, response, exception) -> writeSecurityError(
                                response, HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다.")))
                .build();
    }

    private void writeSecurityError(
            jakarta.servlet.http.HttpServletResponse response,
            HttpStatus status,
            String code,
            String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"success\":false,\"data\":null,\"error\":{" +
                "\"code\":\"" + code + "\",\"message\":\"" + message + "\",\"fieldErrors\":null}}");
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origin:http://localhost:5173,http://127.0.0.1:5173}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
