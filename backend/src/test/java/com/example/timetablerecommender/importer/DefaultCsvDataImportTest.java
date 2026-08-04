package com.example.timetablerecommender.importer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.timetablerecommender.repository.CourseInterestAreaRepository;
import com.example.timetablerecommender.repository.CoursePrerequisiteRepository;
import com.example.timetablerecommender.repository.CourseRepository;
import com.example.timetablerecommender.repository.CourseSectionRepository;
import com.example.timetablerecommender.repository.SectionTimeRepository;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:default-import;MODE=PostgreSQL;NON_KEYWORDS=YEAR;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.csv-import.enabled=false"
})
@ActiveProfiles("test")
class DefaultCsvDataImportTest {

    @Autowired
    private CsvImportService importService;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseSectionRepository sectionRepository;
    @Autowired
    private SectionTimeRepository sectionTimeRepository;
    @Autowired
    private CoursePrerequisiteRepository prerequisiteRepository;
    @Autowired
    private CourseInterestAreaRepository courseInterestAreaRepository;

    @Test
    void importsAllDefaultCsvFiles() {
        importService.importCsvFiles();

        assertThat(courseRepository.count()).isEqualTo(61);
        assertThat(sectionRepository.count()).isEqualTo(44);
        assertThat(sectionTimeRepository.count()).isEqualTo(88);
        assertThat(prerequisiteRepository.count()).isEqualTo(33);
        assertThat(courseInterestAreaRepository.count()).isEqualTo(71);
    }
}
