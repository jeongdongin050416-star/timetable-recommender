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
    private final Resource prerequisiteResource;
    private final Resource interestAreaResource;
    private final CourseCsvParser courseParser;
    private final CourseSectionCsvParser sectionParser;
    private final SectionTimeCsvParser timeParser;
    private final CoursePrerequisiteCsvParser prerequisiteParser;
    private final CourseInterestAreaCsvParser interestAreaParser;
    private final CsvImportPersistence persistence;

    public CsvImportService(
            @Value("${app.csv-import.course-resource:classpath:data/course.csv}") Resource courseResource,
            @Value("${app.csv-import.course-section-resource:classpath:data/course_section.csv}") Resource sectionResource,
            @Value("${app.csv-import.section-time-resource:classpath:data/section_time.csv}") Resource timeResource,
            @Value("${app.csv-import.course-prerequisite-resource:classpath:data/course_prerequisite.csv}") Resource prerequisiteResource,
            @Value("${app.csv-import.course-interest-area-resource:classpath:data/course_interest_area.csv}") Resource interestAreaResource,
            CourseCsvParser courseParser,
            CourseSectionCsvParser sectionParser,
            SectionTimeCsvParser timeParser,
            CoursePrerequisiteCsvParser prerequisiteParser,
            CourseInterestAreaCsvParser interestAreaParser,
            CsvImportPersistence persistence) {
        this.courseResource = courseResource;
        this.sectionResource = sectionResource;
        this.timeResource = timeResource;
        this.prerequisiteResource = prerequisiteResource;
        this.interestAreaResource = interestAreaResource;
        this.courseParser = courseParser;
        this.sectionParser = sectionParser;
        this.timeParser = timeParser;
        this.prerequisiteParser = prerequisiteParser;
        this.interestAreaParser = interestAreaParser;
        this.persistence = persistence;
    }

    @Transactional
    public void importCsvFiles() {
        List<CourseRow> courses = courseParser.parse(courseResource);
        List<CourseSectionRow> sections = sectionParser.parse(sectionResource);
        List<SectionTimeRow> times = timeParser.parse(timeResource);
        List<CoursePrerequisiteRow> prerequisites = prerequisiteParser.parse(prerequisiteResource);
        List<CourseInterestAreaRow> interestAreas = interestAreaParser.parse(interestAreaResource);
        persistence.save(
                courses,
                sections,
                times,
                prerequisites,
                interestAreas,
                fileName(sectionResource),
                fileName(timeResource),
                fileName(prerequisiteResource),
                fileName(interestAreaResource));
    }

    private String fileName(Resource resource) {
        return resource.getFilename() == null ? resource.getDescription() : resource.getFilename();
    }
}
