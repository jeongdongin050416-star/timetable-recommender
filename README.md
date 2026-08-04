# Timetable Recommender

사용자의 이수 과목과 관심 분야를 바탕으로 시간 충돌 없는 시간표를 추천하는 서비스입니다.

현재는 프론트엔드·백엔드·PostgreSQL 실행 환경, 회원가입·로그인, 이수 과목 관리와 추천 과목
API가 구현되어 있습니다. 실제 시간표 조합 생성과 인증 토큰·세션은 아직 구현 전입니다.

## 기술 스택

- 백엔드: Java 21, Spring Boot 3, Gradle
- 프론트엔드: React 19, TypeScript, Vite
- 데이터베이스: PostgreSQL 17
- 로컬 인프라: Docker Compose
- DB 마이그레이션: Flyway
- ORM: JPA/Hibernate

## 사전 준비

- JDK 21
- Node.js 20.19 이상 또는 22.12 이상
- Docker 및 Docker Compose

## 실행 방법

프로젝트 루트에서 PostgreSQL을 먼저 실행합니다.

```bash
docker compose up -d
```

백엔드를 실행합니다.

```bash
cd backend
./gradlew bootRun
```

백엔드는 `http://localhost:8080`에서 실행됩니다.

```bash
curl http://localhost:8080/api/health
```

정상 응답은 다음과 같습니다.

```json
{"success":true,"data":{"status":"ok"},"error":null}
```

새 터미널에서 프론트엔드를 실행합니다.

```bash
cd frontend
npm install
npm run dev
```

브라우저에서 `http://localhost:5173`을 엽니다.

## 종료 방법

백엔드와 프론트엔드는 각 터미널에서 `Ctrl+C`로 종료합니다. PostgreSQL 컨테이너는
프로젝트 루트에서 종료합니다.

```bash
docker compose down
```

DB 데이터 볼륨까지 삭제하려면 다음 명령을 사용합니다. 저장된 데이터가 모두 사라지므로
주의해야 합니다.

```bash
docker compose down -v
```

## 데이터베이스 환경 변수

| 환경 변수 | 기본값 |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/timetable` |
| `DB_USERNAME` | `timetable` |
| `DB_PASSWORD` | `timetable` |

## 기본 CSV 데이터 가져오기

`backend/src/main/resources/data`의 과목, 분반, 수업 시간, 선수 과목, 관심 분야 연결 CSV를
DB에 가져오려면 PostgreSQL을 실행한 뒤 `dev` 프로필과 import 옵션을 켜서 백엔드를 실행합니다.

```bash
docker compose up -d
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev --app.csv-import.enabled=true'
```

같은 명령을 다시 실행해도 동일한 행은 중복 생성되지 않습니다. CSV의 필수 값이나 참조가
잘못되면 파일명과 행 번호를 출력하고 5개 파일의 변경을 모두 롤백합니다. 현재 스키마에 열이
없는 `course_section.csv`의 `professor`와 `section_time.csv`의 `classroom`은 가져오지 않습니다.

자동 테스트는 PostgreSQL 없이 인메모리 H2로 실행할 수 있습니다.

```bash
cd backend
./gradlew test
```

`DefaultCsvDataImportTest`는 실제 기본 CSV 전체를, `CsvImportServiceTest`는 저장과 재실행 시
중복 방지를, `CsvImportRollbackTest`는 잘못된 참조가 있을 때 전체 트랜잭션 롤백과 오류 위치
표시를 검증합니다. 실제 PostgreSQL에 들어간 행은 다음처럼 확인할 수 있습니다.

```bash
docker compose exec postgres psql -U timetable -d timetable -c \
  "SELECT 'course' AS table_name, count(*) FROM course UNION ALL SELECT 'course_section', count(*) FROM course_section UNION ALL SELECT 'section_time', count(*) FROM section_time UNION ALL SELECT 'course_prerequisite', count(*) FROM course_prerequisite UNION ALL SELECT 'course_interest_area', count(*) FROM course_interest_area;"
```

## 문서

- [요구사항](docs/REQUIREMENTS.md)
- [API](docs/API.md)
- [데이터베이스](docs/DATABASE.md)
- [추천 기준](docs/RECOMMENDATION.md)
- [작업 목록](docs/TASKS.md)
- [통합 학습 노트](docs/devlog/STUDY_NOTES.md)
