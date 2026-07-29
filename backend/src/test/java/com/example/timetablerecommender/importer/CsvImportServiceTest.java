package com.example.timetablerecommender.importer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.timetablerecommender.repository.CourseRepository;
import com.example.timetablerecommender.repository.CourseSectionRepository;
import com.example.timetablerecommender.repository.SectionTimeRepository;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:valid-import;MODE=PostgreSQL;NON_KEYWORDS=YEAR;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.csv-import.enabled=false",
        "app.csv-import.course-resource=classpath:import/valid/course.csv",
        "app.csv-import.course-section-resource=classpath:import/valid/course_section.csv",
        "app.csv-import.section-time-resource=classpath:import/valid/section_time.csv"
})
@ActiveProfiles("test")
class CsvImportServiceTest {

    @Autowired
    private CsvImportService importService;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseSectionRepository sectionRepository;
    @Autowired
    private SectionTimeRepository sectionTimeRepository;

    @Test
    void importsFilesAndDoesNotCreateCoursesSectionsOrTimesTwice() {
        importService.importCsvFiles();
        importService.importCsvFiles();

        assertThat(courseRepository.count()).isEqualTo(2);
        assertThat(sectionRepository.count()).isEqualTo(1);
        assertThat(sectionTimeRepository.count()).isEqualTo(1);
    }
}
