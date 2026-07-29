package com.example.timetablerecommender.importer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.timetablerecommender.domain.Course;
import com.example.timetablerecommender.domain.CourseSection;
import com.example.timetablerecommender.domain.SectionTime;
import com.example.timetablerecommender.repository.CourseRepository;
import com.example.timetablerecommender.repository.CourseSectionRepository;
import com.example.timetablerecommender.repository.SectionTimeRepository;

@Component
class CsvImportPersistence {

    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;
    private final SectionTimeRepository sectionTimeRepository;

    CsvImportPersistence(
            CourseRepository courseRepository,
            CourseSectionRepository sectionRepository,
            SectionTimeRepository sectionTimeRepository) {
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.sectionTimeRepository = sectionTimeRepository;
    }

    void save(
            List<CourseRow> courseRows,
            List<CourseSectionRow> sectionRows,
            List<SectionTimeRow> timeRows,
            String sectionFileName,
            String timeFileName) {
        Map<String, Course> courses = new HashMap<>();
        for (CourseRow row : courseRows) {
            Course course = courseRepository.findByCourseCode(row.courseCode())
                    .orElseGet(() -> courseRepository.save(new Course(
                            row.courseCode(), row.name(), row.credits(), row.courseType())));
            courses.put(row.courseCode(), course);
        }

        Map<String, CourseSection> sectionsByKey = new HashMap<>();
        for (CourseSectionRow row : sectionRows) {
            Course course = courses.computeIfAbsent(
                    row.courseCode(),
                    code -> courseRepository.findByCourseCode(code).orElse(null));
            if (course == null) {
                throw new CsvImportException(
                        sectionFileName, row.rowNumber(), "존재하지 않는 courseCode입니다: " + row.courseCode());
            }

            CourseSection section = sectionRepository
                    .findByCourseCourseCodeAndYearAndSemesterAndSectionNumber(
                            row.courseCode(), row.year(), row.semester(), row.sectionNumber())
                    .orElseGet(() -> sectionRepository.save(new CourseSection(
                            course, row.year(), row.semester(), row.sectionNumber())));

            CourseSection previous = sectionsByKey.putIfAbsent(row.sectionKey(), section);
            if (previous != null && !previous.getId().equals(section.getId())) {
                throw new CsvImportException(
                        sectionFileName, row.rowNumber(), "sectionKey가 서로 다른 분반에 중복됩니다: " + row.sectionKey());
            }
        }

        for (SectionTimeRow row : timeRows) {
            CourseSection section = sectionsByKey.get(row.sectionKey());
            if (section == null) {
                throw new CsvImportException(
                        timeFileName, row.rowNumber(), "존재하지 않는 sectionKey입니다: " + row.sectionKey());
            }
            if (!sectionTimeRepository.existsBySectionIdAndDayOfWeekAndStartTimeAndEndTime(
                    section.getId(), row.dayOfWeek(), row.startTime(), row.endTime())) {
                sectionTimeRepository.save(new SectionTime(
                        section, row.dayOfWeek(), row.startTime(), row.endTime()));
            }
        }
    }
}
