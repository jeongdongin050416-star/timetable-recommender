CSV import 기능

애플리케이션 시작
    ↓
CsvImportRunner
    ↓
CsvImportService  ← 전체 트랜잭션 담당
    ↓
┌──────────────────────────────────┐
│ CSV 파싱                          │
│ CourseCsvParser                  │
│ CourseSectionCsvParser           │
│ SectionTimeCsvParser             │
└──────────────────────────────────┘
    ↓
CourseRow / CourseSectionRow / SectionTimeRow
    ↓
CsvImportPersistence
    ↓
CourseRepository / CourseSectionRepository / SectionTimeRepository
    ↓
PostgreSQL

CSV를 읽고 검증하는 코드와 DB에 저장하는 코드를 의도적으로 분리한 구조