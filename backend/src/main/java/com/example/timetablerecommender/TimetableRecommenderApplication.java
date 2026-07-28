package com.example.timetablerecommender;

// 패키지 이름의 마침표(.)는 디렉터리 계층을 구분한다.
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 백엔드 애플리케이션의 시작점이다.
 *
 * @SpringBootApplication이 선언된 이 클래스의 패키지를 기준으로 하위 패키지를
 * 탐색한다. 따라서 config, domain, web 패키지의 Spring 구성 요소와
 * JPA 엔티티가 탐색 대상이 된다.
 */
@SpringBootApplication
public class TimetableRecommenderApplication {

    public static void main(String[] args) {
        // JVM이 main()을 호출하면 SpringApplication.run()이 Spring Boot를 시작한다.
        SpringApplication.run(TimetableRecommenderApplication.class, args);
    }
}

/*
 * 애플리케이션 시작 과정(개념적인 순서)
 * 1. main()에서 Spring Boot를 시작한다.
 * 2. application.yml 등의 설정을 읽는다.
 * 3. Spring이 관리할 구성 요소와 JPA 엔티티를 탐색한다.
 * 4. SecurityConfig와 HealthController 등을 등록한다.
 * 5. application.yml의 DataSource 설정으로 DB에 연결한다.
 * 6. Flyway가 아직 적용하지 않은 마이그레이션을 실행한다.
 * 7. Hibernate가 엔티티 매핑과 DB 스키마를 검증하고 초기화된다.
 * 8. 내장 웹 서버가 8080 포트에서 요청을 기다린다.
 *
 * 세부 초기화 단계 일부는 프레임워크 내부에서 서로 맞물려 진행되므로 위 순서는 이해를
 * 돕기 위한 개념적인 흐름이다.
 */
