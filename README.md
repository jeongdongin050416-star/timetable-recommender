# Timetable Recommender

사용자의 이수 과목과 관심 분야를 바탕으로 시간 충돌 없는 시간표를 추천하는 서비스입니다.

현재는 프론트엔드·백엔드·PostgreSQL 실행 환경, 상태 확인 API, 최초 DB 스키마와 JPA
엔티티까지 구현되어 있습니다. 회원 인증, 과목 관리 API, 추천 알고리즘과 실제 시간표 UI는
아직 구현 전입니다.

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
{"status":"ok"}
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

## 문서

- [요구사항](docs/REQUIREMENTS.md)
- [API](docs/API.md)
- [데이터베이스](docs/DATABASE.md)
- [추천 기준](docs/RECOMMENDATION.md)
- [작업 목록](docs/TASKS.md)
- [통합 학습 노트](docs/devlog/STUDY_NOTES.md)
