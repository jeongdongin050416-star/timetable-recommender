package com.example.timetablerecommender.importer;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CsvImportService {

    private final Resource courseResource;
    private final Resource sectionResource;
    private final Resource timeResource;
    private final CourseCsvParser courseParser;
    private final CourseSectionCsvParser sectionParser;
    private final SectionTimeCsvParser timeParser;
    private final CsvImportPersistence persistence;

    public CsvImportService(
            @Value("${app.csv-import.course-resource:classpath:data/course.csv}") Resource courseResource,
            @Value("${app.csv-import.course-section-resource:classpath:data/course_section.csv}") Resource sectionResource,
            @Value("${app.csv-import.section-time-resource:classpath:data/section_time.csv}") Resource timeResource,
            CourseCsvParser courseParser,
            CourseSectionCsvParser sectionParser,
            SectionTimeCsvParser timeParser,
            CsvImportPersistence persistence) {
        this.courseResource = courseResource;
        this.sectionResource = sectionResource;
        this.timeResource = timeResource;
        this.courseParser = courseParser;
        this.sectionParser = sectionParser;
        this.timeParser = timeParser;
        this.persistence = persistence;
    }

    @Transactional
    public void importCsvFiles() {
        List<CourseRow> courses = courseParser.parse(courseResource);
        List<CourseSectionRow> sections = sectionParser.parse(sectionResource);
        List<SectionTimeRow> times = timeParser.parse(timeResource);
        persistence.save(
                courses,
                sections,
                times,
                fileName(sectionResource),
                fileName(timeResource));
    }

    private String fileName(Resource resource) {
        return resource.getFilename() == null ? resource.getDescription() : resource.getFilename();
    }
}
