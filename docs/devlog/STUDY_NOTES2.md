# CSV import 학습 노트

과목, 분반, 수업 시간, 선수 관계와 관심 분야 연결 CSV를 하나의 트랜잭션으로 가져오는
구조를 정리한 문서입니다.

```text

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
│ CoursePrerequisiteCsvParser      │
│ CourseInterestAreaCsvParser      │
└──────────────────────────────────┘
    ↓
ImportRows의 CSV별 Row record
    ↓
CsvImportPersistence
    ↓
CourseRepository / CourseSectionRepository / SectionTimeRepository
    ↓
PostgreSQL

```

CSV를 읽고 검증하는 코드와 DB에 저장하는 코드를 의도적으로 분리했습니다. 파싱이나 참조
검증에 실패하면 파일명과 행 번호를 포함한 오류를 내고 다섯 CSV의 변경을 모두 롤백합니다.
동일 데이터를 다시 import해도 중복 행은 만들지 않습니다.
