package com.example.timetablerecommender;
//package 이름에서 마침표(.)는 폴더(디렉터리) 경로를 나타내는 구분 기호이다.
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//이 파일이 백엔드 프로그램의 시작점임
//이 코드의 main() 메서드부터 프로그램이 실행됨
@SpringBootApplication
//Spring Boot에서는 보통 @SpringBootApplication이 선언된 메인 클래스의 패키지와
//그 하위 패키지를 스캔함. @Entity가 붙은 클래스가 스캔 범위에 있으면 엔티티로 등록됨.
public class TimetableRecommenderApplication {

    public static void main(String[] args) {
        //SpringApplication.run() 이 코드가 Spring Boot를 가동시킴
        SpringApplication.run(TimetableRecommenderApplication.class, args);
        //Spring Boot가 가동되면?
    }
}
//1. Spring Boot 실행
//2. application.yml 읽기
//3. Spring이 관리할 클래스(config, domain, web 디렉터리 검색) 검색
//4. SecurityConfig 등록
//5. HealthController 등록
//6. Entity 등록
//7. DB 연결
//8. Flyway 실행
//9. Hibernate 초기화
//10. 내장 웹 서버 실행
