app_user
- id                 PK
- login_id           UNIQUE
- password_hash
- name

completed_course
- id                 PK
- user_id            FK → app_user.id
- course_id          FK → course.id
UNIQUE(user_id, course_id)

course
- id                 PK
- course_code        UNIQUE   예: CS360
- name                        예: 데이터베이스 개론
- credits
- course_type                 기초필수, 전공필수, 전공선택 등

Relationship of entities
app_user 1 ─── N completed_course N ─── 1 course

course_section
- id                 PK
- course_id          FK → course.id
- year
- semester
- section_number              예: A, B, 1
UNIQUE(course_id, year, semester, section_number)

section_time
- id                 PK
- section_id         FK → course_section.id
- day_of_week
- start_time
- end_time

course 1 ─── N course_section 1 ─── N section_time

interest_area
- id
- name

interest_area 값들
DATA_SCIENCE
SYSTEM_NETWORK
THEORY
SOFTWARE_DESIGN
SECURE_COMPUTING
VISUAL_COMPUTING
AI_INFORMATION_SERVICE
SOCIAL_COMPUTING
INTERACTIVE_COMPUTING

course_interest_area
- course_id          FK → course.id
- interest_area_id   FK → interest_area.id

course_prerequisite
- course_id
- prerequisite_course_id
- relation_type

relation_type: PREREQUISITE, RECOMMENDED