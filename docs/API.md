# 백엔드 API

기본 주소는 `http://localhost:8080`입니다. 상태 확인, 회원가입, 로그인을 제외한 `/api/**`
요청에는 로그인으로 발급된 `JSESSIONID` 쿠키가 필요합니다. 모든 성공과 실패 응답은 다음
최상위 구조를 사용합니다.

인증 쿠키가 없거나 만료되면 `401 UNAUTHORIZED`, 로그인은 되었지만 필요한 권한이 없으면
`403 FORBIDDEN`을 반환합니다. 사용자 데이터 API는 URL이나 요청 본문의 사용자 ID를 신뢰하지
않고 로그인 세션의 사용자 ID만 사용합니다.

```json
{"success":true,"data":{},"error":null}
```

```json
{"success":false,"data":null,"error":{"code":"ERROR_CODE","message":"오류 메시지","fieldErrors":null}}
```

Validation 오류는 `INVALID_REQUEST`이며 `fieldErrors`에 필드별 메시지가 들어갑니다. 예상하지
못한 오류는 내부 구현을 노출하지 않고 `INTERNAL_SERVER_ERROR`로 응답합니다.

## 상태 확인

### `GET /api/health`

- 성공: `200 OK`
- 요청 값: 없음
- 멱등: 예

```json
{"success":true,"data":{"status":"ok"},"error":null}
```

```bash
curl "http://localhost:8080/api/health"
```

## 회원가입

### `POST /api/auth/signup`

- Body: `email`, `password`, `name` 모두 필수
- Validation: 올바른 이메일, 비밀번호 8자 이상, 공백이 아닌 이름
- 성공: `201 Created`
- 실패: `400 INVALID_REQUEST`, `409 DUPLICATE_USER`
- 멱등: 아니요. 같은 이메일의 두 번째 요청은 409입니다.
- 비밀번호는 BCrypt 해시로만 저장되며 응답에 포함되지 않습니다.

```bash
curl -X POST "http://localhost:8080/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123!","name":"홍길동"}'
```

성공 응답:

```json
{"success":true,"data":{"userId":1,"email":"user@example.com","name":"홍길동"},"error":null}
```

Validation 실패 예:

```bash
curl -X POST "http://localhost:8080/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid","password":"short","name":""}'
```

```json
{"success":false,"data":null,"error":{"code":"INVALID_REQUEST","message":"요청 값이 올바르지 않습니다.","fieldErrors":{"email":"올바른 이메일 형식이어야 합니다.","password":"비밀번호는 8자 이상이어야 합니다.","name":"이름은 필수입니다."}}}
```

## 로그인

### `POST /api/auth/login`

- Body: `email`, `password` 필수
- 성공: `200 OK`
- 실패: `400 INVALID_REQUEST`, `401 INVALID_CREDENTIALS`
- 이메일 미존재와 비밀번호 불일치는 같은 오류로 응답합니다.
- 로그인 성공 시 서버 세션을 만들고 HttpOnly `JSESSIONID` 쿠키를 발급합니다.

```bash
curl -c cookie.txt -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123!"}'
```

```json
{"success":true,"data":{"userId":1,"email":"user@example.com","name":"홍길동"},"error":null}
```

로그인 실패:

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"wrong-password"}'
```

```json
{"success":false,"data":null,"error":{"code":"INVALID_CREDENTIALS","message":"이메일 또는 비밀번호가 올바르지 않습니다.","fieldErrors":null}}
```

## 전체 과목 목록

### `GET /api/courses`

- 성공: `200 OK`; 과목 코드 오름차순, 없으면 빈 배열
- 요청 값: 없음
- 멱등: 예
- DB에 CSV import로 적재된 과목을 응답 DTO로 반환합니다.

```bash
curl -b cookie.txt "http://localhost:8080/api/courses"
```

```json
{"success":true,"data":{"courses":[{"courseCode":"CS101","name":"프로그래밍 기초","credits":2}]},"error":null}
```

## 이수 과목 목록

### `GET /api/users/{userId}/completed-courses`

- Path: `userId` 사용자 ID
- 성공: `200 OK`; 과목 코드 오름차순, 없으면 빈 배열
- 실패: `404 USER_NOT_FOUND`
- 멱등: 예

```bash
curl "http://localhost:8080/api/users/1/completed-courses"
```

```json
{"success":true,"data":{"userId":1,"courses":[{"courseCode":"CS101","name":"프로그래밍 기초","credits":3}]},"error":null}
```

존재하지 않는 사용자:

```bash
curl "http://localhost:8080/api/users/999999/completed-courses"
```

```json
{"success":false,"data":null,"error":{"code":"USER_NOT_FOUND","message":"사용자를 찾을 수 없습니다.","fieldErrors":null}}
```

## 이수 과목 추가

### `PUT /api/users/{userId}/completed-courses/{courseCode}`

- Path: `userId`, `courseCode`(예: `CS101`)
- 성공: 항상 `200 OK`
- 실패: `404 USER_NOT_FOUND`, `404 COURSE_NOT_FOUND`
- 멱등: 예. 이미 등록된 관계는 추가하지 않으며 DB 유니크 제약도 중복을 방지합니다.

```bash
curl -X PUT "http://localhost:8080/api/users/1/completed-courses/CS101"
curl -X PUT "http://localhost:8080/api/users/1/completed-courses/CS101"
```

```json
{"success":true,"data":{"userId":1,"courseCode":"CS101","completed":true},"error":null}
```

## 이수 과목 삭제

### `DELETE /api/users/{userId}/completed-courses/{courseCode}`

- Path: `userId`, `courseCode`(예: `CS101`)
- 성공: 항상 `200 OK`
- 실패: `404 USER_NOT_FOUND`, `404 COURSE_NOT_FOUND`
- 멱등: 예. 관계가 없어도 성공합니다.

```bash
curl -X DELETE "http://localhost:8080/api/users/1/completed-courses/CS101"
curl -X DELETE "http://localhost:8080/api/users/1/completed-courses/CS101"
```

```json
{"success":true,"data":{"userId":1,"courseCode":"CS101","completed":false},"error":null}
```

## 추천 시간표 조합

### `GET /api/users/{userId}/recommended-timetables`

- Path: `userId`
- Query: `targetCourseCount` 필수, 1~20. 각 시간표 조합에 포함할 정확한 과목 수
- Query: `interestedAreaIds`, `uninterestedAreaIds` 선택; 쉼표로 구분한 관심 분야 ID
- 성공: `200 OK`; 정확한 과목 수로 만들 수 없으면 `timetable`을 `null`로 반환
- 실패: `400 INVALID_REQUEST`, `404 USER_NOT_FOUND`, `404 INTEREST_AREA_NOT_FOUND`
- 멱등: 예

이미 이수한 과목은 후보에서 제외합니다. DFS 백트래킹으로 서로 다른 과목마다 분반 하나를
선택하며 모든 수업 시간이 충돌하지 않는 정확히 `targetCourseCount`개 과목의 조합을 만듭니다.
점수가 가장 높은 시간표 조합 하나만 반환하며, 동점은 과목 코드와 분반 키 순으로 결정합니다.

- 관심 분야에 속하는 과목: 과목당 `+30`
- 미관심 분야에 속하는 과목: 과목당 `-15`
- 전공필수: 과목당 `+20`
- `RECOMMENDED` 선수 관계: 이수했으면 관계당 `+15`, 미이수면 `0`
- `PREREQUISITE` 선수 관계: 이수했으면 관계당 `+20`, 미이수면 `-20`

```bash
curl "http://localhost:8080/api/users/1/recommended-timetables?targetCourseCount=3&interestedAreaIds=1,2&uninterestedAreaIds=3"
```

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "targetCourseCount": 3,
    "timetable": {
      "score": 69,
      "courseCount": 3,
      "courses": [
        {
          "courseCode": "CS201",
          "name": "데이터 구조",
          "credits": 3,
          "sectionKey": "CS201-2026-FALL-A",
          "meetingTimes": [{"dayOfWeek":"MONDAY","startTime":"09:00:00","endTime":"10:30:00"}, 
            {"dayOfWeek":"WEDNESDAY","startTime":"09:00:00","endTime":"10:30:00"}]
        },
        {
          "courseCode": "CS300",
          "name": "알고리즘 개론",
          "credits": 3,
          "sectionKey": "CS300-2026-FALL-A",
          "meetingTimes": [{"dayOfWeek":"TUESDAY","startTime":"09:00:00","endTime":"10:30:00"},
            {"dayOfWeek":"THURSDAY","startTime":"09:00:00","endTime":"10:30:00"}]
        },
        {
          "courseCode": "CS360",
          "name": "데이터베이스 개론",
          "credits": 3,
          "sectionKey": "CS360-2026-FALL-A",
          "meetingTimes": [{"dayOfWeek":"MONDAY","startTime":"09:00:00","endTime":"10:30:00"},
            {"dayOfWeek":"WEDNESDAY","startTime":"09:00:00","endTime":"10:30:00"}]
        }
      ]
    }
  },
  "error": null
}
```

유효성 검증 실패:

```bash
curl "http://localhost:8080/api/users/1/recommended-timetables?targetCourseCount=0"
```

```json
{"success":false,"data":null,"error":{"code":"INVALID_REQUEST","message":"요청 값이 올바르지 않습니다.","fieldErrors":{"targetCourseCount":"1 이상이어야 합니다."}}}
```
