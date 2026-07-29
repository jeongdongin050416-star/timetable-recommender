package com.example.timetablerecommender.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.timetablerecommender.repository.CourseRepository;
import com.example.timetablerecommender.repository.CourseSectionRepository;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:rollback-import;MODE=PostgreSQL;NON_KEYWORDS=YEAR;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.csv-import.enabled=false",
        "app.csv-import.course-resource=classpath:import/rollback/course.csv",
        "app.csv-import.course-section-resource=classpath:import/rollback/course_section.csv",
        "app.csv-import.section-time-resource=classpath:import/rollback/section_time.csv"
})
@ActiveProfiles("test")
class CsvImportRollbackTest {

    @Autowired
    private CsvImportService importService;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseSectionRepository sectionRepository;

    @Test
    void rollsBackEverythingAndReportsFileAndRowWhenOneRowIsInvalid() {
        assertThatThrownBy(importService::importCsvFiles)
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("section_time.csv")
                .hasMessageContaining("row 2");

        assertThat(courseRepository.count()).isZero();
        assertThat(sectionRepository.count()).isZero();
    }
}
