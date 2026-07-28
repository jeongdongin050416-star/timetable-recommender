# 데이터베이스 설계

최초 스키마는
`backend/src/main/resources/db/migration/V1__create_schema.sql`에서 관리합니다. Flyway는
실행 여부를 `flyway_schema_history`에 기록하므로, 적용된 파일을 수정하지 않고 새로운
버전의 마이그레이션을 추가해야 합니다.

## 관계 요약

```text
app_user 1 ── N completed_course N ── 1 course
course 1 ── N course_section 1 ── N section_time
course N ── N interest_area       (course_interest_area를 통해 연결)
course N ── N course              (course_prerequisite의 자기 참조 관계)
```

## 테이블

### `app_user`

| 열 | 제약 조건 | 설명 |
| --- | --- | --- |
| `id` | PK, 자동 생성 | 사용자 식별자 |
| `login_id` | NOT NULL, UNIQUE | 로그인 ID |
| `password_hash` | NOT NULL | 해시된 비밀번호 |
| `name` | NOT NULL | 사용자 이름 |

### `course`

| 열 | 제약 조건 | 설명 |
| --- | --- | --- |
| `id` | PK, 자동 생성 | 과목 식별자 |
| `course_code` | NOT NULL, UNIQUE | 과목 코드(예: `CS360`) |
| `name` | NOT NULL | 과목명(예: 데이터베이스 개론) |
| `credits` | NOT NULL, `> 0` | 학점 |
| `course_type` | NOT NULL | 기초필수, 전공필수, 전공선택 등 |

### `completed_course`

사용자와 이수 과목을 연결합니다.

| 열 | 제약 조건 | 설명 |
| --- | --- | --- |
| `id` | PK, 자동 생성 | 이수 기록 식별자 |
| `user_id` | FK → `app_user.id` | 사용자 |
| `course_id` | FK → `course.id` | 이수 과목 |

`(user_id, course_id)`는 UNIQUE이므로 한 사용자가 같은 과목을 중복 등록할 수 없습니다.

### `course_section`

과목의 연도·학기별 분반을 나타냅니다.

| 열 | 제약 조건 | 설명 |
| --- | --- | --- |
| `id` | PK, 자동 생성 | 분반 식별자 |
| `course_id` | FK → `course.id` | 과목 |
| `year` | NOT NULL, `> 0` | 개설 연도 |
| `semester` | NOT NULL | 개설 학기 |
| `section_number` | NOT NULL | 분반 번호(예: `A`, `B`, `1`) |

`(course_id, year, semester, section_number)` 조합은 UNIQUE입니다.

### `section_time`

한 분반의 요일별 수업 시간을 나타냅니다. 한 분반이 여러 요일에 열릴 수 있으므로 분반과
수업 시간은 1:N 관계입니다.

| 열 | 제약 조건 | 설명 |
| --- | --- | --- |
| `id` | PK, 자동 생성 | 수업 시간 식별자 |
| `section_id` | FK → `course_section.id` | 분반 |
| `day_of_week` | NOT NULL, 허용값 검사 | 요일 |
| `start_time` | NOT NULL | 시작 시간 |
| `end_time` | NOT NULL | 종료 시간 |

요일은 `MONDAY`부터 `SUNDAY`까지 허용하며, 시작 시간은 종료 시간보다 빨라야 합니다.

### `interest_area`

| 열 | 제약 조건 | 설명 |
| --- | --- | --- |
| `id` | PK, 자동 생성 | 관심 분야 식별자 |
| `name` | NOT NULL, UNIQUE | 관심 분야 이름 |

초기 관심 분야:

- `DATA_SCIENCE`
- `SYSTEM_NETWORK`
- `THEORY`
- `SOFTWARE_DESIGN`
- `SECURE_COMPUTING`
- `VISUAL_COMPUTING`
- `AI_INFORMATION_SERVICE`
- `SOCIAL_COMPUTING`
- `INTERACTIVE_COMPUTING`

### `course_interest_area`

과목과 관심 분야의 N:M 관계를 연결하는 테이블입니다. 별도 `id` 없이
`(course_id, interest_area_id)`를 복합 기본 키로 사용합니다.

### `course_prerequisite`

한 과목과 그 선수·권장 과목을 연결하는 자기 참조 관계입니다.

| 열 | 제약 조건 | 설명 |
| --- | --- | --- |
| `course_id` | PK 일부, FK → `course.id` | 수강하려는 과목 |
| `prerequisite_course_id` | PK 일부, FK → `course.id` | 선수 또는 권장 과목 |
| `relation_type` | NOT NULL, 허용값 검사 | 관계 종류 |

`relation_type`은 필수 선수 과목을 뜻하는 `PREREQUISITE` 또는 권장 과목을 뜻하는
`RECOMMENDED`입니다. 한 과목이 자기 자신의 선수 과목이 될 수 없습니다.
