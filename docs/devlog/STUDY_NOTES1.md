# 통합 학습 노트

이 문서는 2026-07-26과 2026-07-28 개발일지, 그리고 소스 코드에 작성한 주석의 학습
내용을 한곳에 모은 것이다.

## 1. 프로젝트의 목표와 현재 상태

이 프로젝트는 사용자의 이수 과목, 관심 분야와 학년을 바탕으로 시간 충돌 없는 시간표를
추천하는 서비스이다.

현재 구현된 범위:

- React·TypeScript·Vite 기반 로그인, 로드맵과 주간 시간표 화면
- Java 21·Spring Boot·Gradle 백엔드
- `GET /api/health` 상태 확인 API
- Docker Compose 기반 PostgreSQL
- Flyway 버전 마이그레이션과 CSV 기본 데이터 import
- DB 테이블에 대응하는 JPA 엔티티
- 세션 기반 회원가입·로그인·로그아웃과 `ROLE_USER` API 보호
- 사용자별 이수 과목 관리와 추천 엔진

현재 추천 API는 최고 점수 시간표 하나를 반환한다. 관심 분야 영구 저장과 상위 시간표 최대
5개 비교는 후속 범위이다.

## 2. 시스템 구성과 요청 흐름

```text
사용자 브라우저
      ↓
React 프론트엔드 (localhost:5173)
      ↓ HTTP API 요청
Spring Boot 백엔드 (localhost:8080)
      ↓ JDBC/JPA로 조회·저장
PostgreSQL (localhost:5432)
```

프론트엔드는 npm으로, 백엔드는 Gradle로 호스트에서 직접 실행한다. PostgreSQL 서버만
Docker 컨테이너에서 실행한다.

## 3. 프론트엔드와 npm

```bash
cd frontend
npm install
npm run dev
```

- `package.json`은 프로젝트 이름, 라이브러리와 실행 스크립트를 정의한다.
- `npm install`은 `package.json`을 읽어 React, Vite 등의 라이브러리를
  `node_modules`에 설치한다.
- `npm run dev`는 `scripts.dev`에 정의된 `vite`를 실행하고 개발 서버를 시작한다.
- `App.tsx`는 현재 화면의 내용을 정의하고 `main.tsx`가 `root` 요소에 렌더링한다.

## 4. Gradle과 Spring Boot 실행

Gradle은 Java 프로젝트를 빌드·실행하고 라이브러리를 관리한다. Node.js에서 npm이 하는
역할과 비슷하다.

```bash
cd backend
./gradlew bootRun
```

Gradle Wrapper:

- `gradlew`: Linux, macOS, WSL용 실행 스크립트
- `gradlew.bat`: Windows 명령 프롬프트와 PowerShell용 실행 스크립트
- Wrapper를 사용하면 별도로 Gradle을 설치하지 않아도 프로젝트가 지정한 버전을 쓸 수 있다.

주요 Gradle 파일:

- `settings.gradle`: 프로젝트 이름을 `timetable-recommender-backend`로 지정한다.
- `build.gradle`: Java 21, Spring Boot 플러그인, 저장소, 의존성과 테스트 설정을 정의한다.
- `repositories { mavenCentral() }`: 라이브러리를 받을 저장소를 지정한다.
- `dependencies`: 컴파일·실행·테스트에 필요한 라이브러리와 사용 범위를 선언한다.

주요 의존성:

- Spring Web: HTTP API와 내장 웹 서버
- Spring Data JPA: JPA, Hibernate와 Repository 기반 DB 접근
- Validation: 입력값 검증
- Spring Security: 인증·인가와 보안 필터
- Flyway: DB 마이그레이션
- PostgreSQL Driver: Java와 PostgreSQL의 JDBC 연결
- Spring Boot Test, JUnit Platform: 테스트

`./gradlew bootRun`의 흐름:

1. `build.gradle`을 읽고 필요한 라이브러리를 준비한다.
2. `src/main/java` 아래 Java 파일을 재귀적으로 찾아 `.class`로 컴파일한다.
3. `application.yml` 등 `src/main/resources`의 리소스를 포함한다.
4. JVM이 `TimetableRecommenderApplication.main()`을 호출한다.
5. `SpringApplication.run()`이 Spring Boot를 시작한다.
6. 애플리케이션이 8080 포트에서 요청을 기다린다.

## 5. Spring Boot 시작 과정

`TimetableRecommenderApplication`은 백엔드의 시작점이다. 패키지 이름의 마침표는
디렉터리 계층을 구분한다.

`@SpringBootApplication`이 선언된 `com.example.timetablerecommender`를 기준으로 하위
패키지를 탐색하므로 `config`, `domain`, `web`이 범위에 포함된다.

개념적인 초기화 흐름:

1. `main()`에서 Spring Boot를 시작한다.
2. `application.yml` 같은 설정 파일을 읽는다.
3. Spring 구성 요소와 JPA 엔티티를 탐색한다.
4. `SecurityConfig`, `HealthController` 등을 등록한다.
5. DataSource 설정으로 PostgreSQL에 연결한다.
6. Flyway가 아직 적용하지 않은 마이그레이션을 실행한다.
7. Hibernate가 엔티티 매핑과 실제 DB 스키마를 검증한다.
8. 내장 웹 서버가 8080 포트에서 요청을 기다린다.

세부 초기화 단계는 프레임워크 내부에서 서로 맞물려 진행되므로 위 순서는 이해를 위한
개념적 흐름이다.

컴파일, 발견, 객체 생성, 메서드 실행은 서로 다른 단계이다. 모든 Java 파일은 컴파일될 수
있지만 모든 객체나 메서드가 서버 시작과 동시에 생성·실행되는 것은 아니다.

## 6. Java 애너테이션

`@Something` 형태의 애너테이션은 클래스, 메서드, 필드에 붙이는 부가 정보이다. 일반
메서드 호출처럼 스스로 실행되는 것이 아니라 각 도구가 읽고 처리한다.

| 읽는 주체 | 예시 | 역할 |
| --- | --- | --- |
| Spring | `@SpringBootApplication`, `@Configuration`, `@Bean` | 애플리케이션·설정·객체 등록 |
| Spring MVC | `@RestController`, `@RequestMapping`, `@GetMapping` | HTTP 경로와 메서드 연결 |
| JPA/Hibernate | `@Entity`, `@Table`, `@Id`, `@Column`, `@ManyToOne` | 객체와 테이블 매핑 |
| Java 컴파일러 | `@Override` | 상위 타입 메서드를 올바르게 재정의했는지 검사 |

## 7. HTTP 요청과 상태 확인 API

`@RestController`는 `HealthController`를 HTTP 요청 처리 컨트롤러로 등록한다.
`@RequestMapping("/api")`는 클래스의 모든 경로 앞에 `/api`를 붙이고,
`@GetMapping("/health")`는 GET 요청을 `health()`와 연결한다.

서버 시작 시 컨트롤러 객체와 경로 연결을 준비할 뿐 `health()`를 즉시 실행하지 않는다.

```text
GET /api/health
      ↓
SecurityFilterChain
      ↓
Spring MVC가 연결된 health() 탐색
      ↓
HealthController.health() 실행
      ↓
ApiResponse.success(...) 반환
      ↓
Spring이 응답 DTO를 JSON으로 변환
      ↓
{"success":true,"data":{"status":"ok"},"error":null}
```

`SecurityConfig`의 `@Configuration`은 Spring 설정 클래스임을 뜻하고 `@Bean`은
메서드가 반환한 `SecurityFilterChain`을 Spring이 관리하도록 등록한다. 상태 확인,
회원가입과 로그인만 공개하며 나머지 `/api/**`는 `ROLE_USER` 세션을 요구한다. 인증 실패와
권한 부족은 각각 공통 JSON 형식의 401, 403 응답으로 처리한다. 브라우저 요청은 허용된
CORS origin과 HttpOnly·SameSite 세션 쿠키를 사용한다.

## 8. Docker와 Docker Compose

Docker는 프로그램과 그 실행 환경을 컨테이너라는 격리된 단위로 묶어 실행한다. Docker
Compose는 여러 컨테이너의 이미지, 환경 변수, 포트, 볼륨, 네트워크 등을 하나의 YAML
파일로 정의하고 관리한다. 현재는 PostgreSQL 컨테이너 하나를 관리한다.

```bash
docker compose up -d   # 서비스 생성 및 백그라운드 실행
docker compose down    # 컨테이너와 네트워크 종료·제거
docker compose down -v # 이름 있는 데이터 볼륨까지 제거
```

`docker-compose.yml`의 핵심:

- `image: postgres:17-alpine`: 컨테이너를 만들 PostgreSQL 원본 이미지
- `environment`: 최초 DB 이름, 사용자, 비밀번호
- `ports: "5432:5432"`: 호스트 5432 포트를 컨테이너 5432 포트로 전달
- `volumes`: 컨테이너를 삭제해도 DB 데이터를 유지하는 `postgres-data` 볼륨
- `healthcheck`: `pg_isready`로 프로세스 실행 여부를 넘어 실제 DB 접속 준비 상태 확인

YAML에서 `key: value`는 설정값, 들여쓰기는 포함 관계, `-`는 목록이다. `.yml`과
`.yaml`은 같은 형식이다. YAML 자체가 실행되는 것이 아니라 Docker Compose나 Spring
Boot 같은 프로그램이 읽을 때 의미가 생긴다.

## 9. application.yml과 DB 접속

Spring Boot는 `application.properties`, `application.yml`, `application.yaml`을 설정
파일로 인식하며 정해진 클래스패스·외부 설정 위치에서 찾는다.

`${DB_URL:기본값}`은 `DB_URL` 환경 변수가 있으면 그 값을, 없으면 콜론 뒤 기본값을
사용한다. 기본 접속 정보는 다음과 같다.

| 항목 | 값 |
| --- | --- |
| 주소 | `localhost` |
| 포트 | `5432` |
| DB | `timetable` |
| 사용자 | `timetable` |
| 비밀번호 | `timetable` |

```text
application.yml의 JDBC URL
        ↓
호스트 localhost:5432
        ↓ Docker 포트 전달
PostgreSQL 컨테이너:5432
        ↓
timetable 데이터베이스
```

`server.port: 8080`은 백엔드 HTTP 포트다. `spring.jpa.open-in-view: false`는 HTTP 요청
전체가 끝날 때까지 영속성 컨텍스트를 열어 두는 기본 동작을 끈다.

## 10. ORM, JPA와 Hibernate

- ORM(Object-Relational Mapping): Java 객체와 관계형 DB 테이블을 연결하는 기술
- JPA: Java에서 ORM을 사용하는 방법을 정의한 표준 규칙
- Hibernate: JPA 규칙을 실제로 구현한 프레임워크

`ddl-auto: validate`는 Hibernate가 DB 테이블을 직접 만들거나 바꾸지 않고 엔티티 매핑과
실제 스키마가 일치하는지만 검사하도록 한다. 엔티티에 대응하는 테이블이나 열이 없거나
타입이 맞지 않으면 시작이 실패한다.

이 프로젝트의 역할 분담:

- Flyway: SQL로 DB 구조(DDL)를 생성·변경하고 이력을 관리
- Hibernate: 엔티티 매핑 검증, Java 객체와 DB 행의 변환, 영속성 컨텍스트 관리

DB 연결과 매핑 흐름:

1. `@Entity` 클래스를 찾아 클래스·필드와 테이블·열의 매핑 정보를 만든다.
2. DataSource로 PostgreSQL에 접속한다.
3. Flyway가 미적용 마이그레이션을 실행한다.
4. Hibernate가 엔티티 매핑과 실제 스키마를 비교한다.
5. 검증 후 `EntityManagerFactory`를 준비한다.
6. Repository나 EntityManager로 데이터를 사용한다.

`EntityManagerFactory`는 생성 비용이 큰 EntityManager 생성 공장이므로 일반적으로
애플리케이션에서 하나를 공유한다.

EntityManager의 핵심 역할:

1. `persist()`, `find()`, `remove()` 같은 작업을 INSERT, SELECT, DELETE 등의 SQL과
   연결한다.
2. 1차 캐시를 운영하고 같은 영속성 컨텍스트에서 동일 엔티티를 재사용한다.
3. 변경 감지(dirty checking)로 수정된 엔티티에 필요한 UPDATE를 반영한다.
4. 객체 중심 Java와 표 중심 관계형 DB 사이의 패러다임 차이를 완화한다.

## 11. 엔티티의 생성과 저장

엔티티 클래스는 Hibernate가 알아서 소스 파일을 만들어 주는 것이 아니다. 개발자가 DB
설계에 맞춰 직접 작성한다.

```text
new AppUser(...)
        ↓
아직은 단순 Java 객체
        ↓
repository.save(user) 또는 entityManager.persist(user)
        ↓
Hibernate의 관리 대상
        ↓
flush/commit
        ↓
INSERT SQL 실행
        ↓
DB 저장
```

따라서 `new`로 객체를 만드는 것만으로 DB에 저장되지는 않는다.

JPA 엔티티에는 기본 생성자가 필요하다. Hibernate가 DB 조회 결과로 객체를 만들 때 먼저
빈 객체를 생성한 다음 필드에 값을 채우기 때문이다. 기본 생성자를 `protected`로 두면
JPA 요구 사항을 만족하면서 애플리케이션 코드가 값 없는 엔티티를 함부로 만드는 일을
막을 수 있다. `public` 생성자는 필수 값을 갖춘 객체를 만들 때 사용한다.

주요 매핑:

- `@Entity`: JPA/Hibernate가 관리하는 엔티티
- `@Table`: 연결할 테이블과 UNIQUE 제약 조건
- `@Id`: 엔티티 식별자 및 DB 기본 키
- `@GeneratedValue(strategy = IDENTITY)`: DB가 ID 생성
- `@Column`: 필드와 열의 이름, null 허용 여부와 길이
- `@ManyToOne`: N:1 관계
- `@JoinColumn`: 외래 키 열
- `fetch = LAZY`: 연관 객체를 실제 필요할 때 조회
- `@Enumerated(STRING)`: enum 이름을 문자열로 저장
- `@EmbeddedId`: 여러 열로 된 복합 기본 키
- `@Embeddable`: 복합 키 값 타입
- `@MapsId`: 연관 객체의 ID를 복합 키의 해당 부분과 연결

## 12. 엔티티와 DB 관계

```text
app_user 1 ── N completed_course N ── 1 course
course 1 ── N course_section 1 ── N section_time
course N ── N interest_area       (course_interest_area)
course N ── N course              (course_prerequisite)
```

- `AppUser`: 로그인 ID, 비밀번호 해시, 이름
- `Course`: 과목 코드, 이름, 학점, 과목 유형
- `CompletedCourse`: 사용자와 이수 과목을 연결하며 조합 중복 금지
- `CourseSection`: 과목의 연도·학기·분반
- `SectionTime`: 분반의 요일, 시작·종료 시간
- `InterestArea`: 관심 분야
- `CourseInterestArea`: 과목과 관심 분야를 복합 키로 연결
- `CoursePrerequisite`: 과목과 선수·권장 과목을 복합 키로 연결
- `RelationType`: `PREREQUISITE`, `RECOMMENDED` 또는 `INCOMPATIBLE`

DB는 양수 학점·연도, 올바른 요일, 시작 시간보다 늦은 종료 시간, 자기 자신이 아닌 선수
과목 같은 CHECK 제약으로 잘못된 데이터도 막는다.

## 13. Flyway

Flyway는 DB 구조 변경 내역을 코드처럼 버전 관리한다.

1. `src/main/resources/db/migration`에서 규칙에 맞는 SQL 파일을 찾는다.
2. application.yml의 DataSource로 DB에 연결한다.
3. `flyway_schema_history`에서 적용 이력을 확인한다.
4. 아직 실행하지 않은 마이그레이션만 버전 순서대로 실행한다.
5. 성공 결과를 이력 테이블에 기록해 중복 실행과 순서 꼬임을 방지한다.

`V1__create_schema.sql`은 최초 구조인 버전 1을 만든다. 이미 공유 DB에 적용된
마이그레이션은 수정하지 않고 변경 사항을 담은 새 버전 SQL 파일을 추가해야 한다.

## 14. 추천 조건

하드 제약 조건은 반드시 만족해야 한다.

- 시간 충돌 금지
- 과목당 하나의 분반만 선택
- 이수 과목 제외
- `INCOMPATIBLE` 관계 과목 동시 선택 금지
- 목표 과목 수 만족

소프트 제약 조건은 후보의 우선순위를 정한다.

- 관심 분야 일치
- 전공필수 우선
- 미관심 분야 감점
- 학년·과목 번호 가점
- 선수·권장 과목 미이수 감점

## 15. Spring Boot를 사용하는 이유

1. 서버를 빠르게 만들 수 있다.
2. 복잡한 설정을 자동화한다.
3. 계층 구조를 표준화하기 좋다.
4. DB 접근 코드를 줄일 수 있다.
5. 트랜잭션 처리가 편리하다.
6. 보안 기능이 강하다.
7. 대규모 프로젝트에 적합하다.
8. 테스트하기 좋다.
9. 생태계가 크다.

## 16. 중요 파일

- `frontend/package.json`: 프론트엔드 의존성과 실행 명령
- `frontend/src/App.tsx`: 현재 화면
- `docker-compose.yml`: PostgreSQL 컨테이너
- `backend/settings.gradle`: Gradle 프로젝트 이름
- `backend/build.gradle`: Java 버전, 플러그인과 의존성
- `backend/src/main/resources/application.yml`: 서버·DB·JPA·Flyway 설정
- `backend/src/main/resources/db/migration/`: 버전별 DB 스키마 변경
- `TimetableRecommenderApplication.java`: 백엔드 시작점
- `SecurityConfig.java`: 현재 보안 필터 설정
- `auth/`: 회원가입·로그인·세션 사용자 처리
- `HealthController.java`: 상태 확인 API
- `recommendation/`: 추천 조건, 점수 계산, 충돌 검사와 API
- `domain/*.java`: DB 테이블에 대응하는 엔티티와 복합 키

## 17. Spring Boot 백엔드 구성
보통 Controller-Service-Repository 분리 구조를 가짐
분리된 구조를 갖고 있어서 수정 및 개발이 편함
1) Controller:
HTTP 요청을 받음
입력값을 해석함
Service를 호출함
결과를 HTTP 응답으로 반환함
2) Service:
실제 기능과 업무 규칙 처리
여러 Repository 작업 조합
트랜잭션 관리
Entity를 DTO로 변환
3) Repository:
DB 저장
DB 조회
DB 삭제
검색 조건 정의

이러한 구조는 크게 2가지로 나뉨
1. package-by-layer
com.example.timetablerecommender/
├─ controler(web)/ # HTTP 요청·응답
├─ service/        # 업무 로직
├─ repository/     # DB 접근
├─ domain/         # 핵심 데이터와 규칙
├─ dto/            # API 요청·응답 형식
└─ config/         # 프레임워크 설정
장점: 초반에 이해하기 쉬움
단점: 기능이 많아지면 관련 파일이 흩어짐

요청 흐름
-------------------------
프론트엔드
   ↓ HTTP 요청
Controller
   ↓ 메서드 호출
Service
   ↓ DB 작업 요청
Repository
   ↓
Hibernate/JPA
   ↓ SQL
PostgreSQL
--------------------------
응답 흐름
--------------------------
PostgreSQL
→ Entity
→ Service
→ Response DTO
→ Controller
→ JSON
→ 프론트엔드
--------------------------
2. package-by-feature 구조
규모가 커지면 파일 배치를 기능 중심으로 바꿈
예시
com.example.timetablerecommender/
├─ course/
│  ├─ CourseController.java
│  ├─ CourseService.java
│  ├─ CourseRepository.java
│  ├─ Course.java
│  ├─ CourseRequest.java
│  └─ CourseResponse.java
│
├─ user/
│  ├─ UserController.java
│  ├─ UserService.java
│  ├─ UserRepository.java
│  └─ User.java
│
├─ schedule/
│  ├─ ScheduleController.java
│  ├─ ScheduleService.java
│  └─ ScheduleRepository.java
│
└─ config/
   └─ SecurityConfig.java
장점: 한 기능의 파일이 한 곳에 모임
단점: 처음에는 폴더 구성이 조금 복잡해 보임
