package com.example.timetablerecommender.importer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.timetablerecommender.domain.Course;
import com.example.timetablerecommender.domain.CourseInterestArea;
import com.example.timetablerecommender.domain.CourseInterestAreaId;
import com.example.timetablerecommender.domain.CoursePrerequisite;
import com.example.timetablerecommender.domain.CoursePrerequisiteId;
import com.example.timetablerecommender.domain.CourseSection;
import com.example.timetablerecommender.domain.InterestArea;
import com.example.timetablerecommender.domain.SectionTime;
import com.example.timetablerecommender.repository.CourseInterestAreaRepository;
import com.example.timetablerecommender.repository.CoursePrerequisiteRepository;
import com.example.timetablerecommender.repository.CourseRepository;
import com.example.timetablerecommender.repository.CourseSectionRepository;
import com.example.timetablerecommender.repository.InterestAreaRepository;
import com.example.timetablerecommender.repository.SectionTimeRepository;

@Component
class CsvImportPersistence {

    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;
    private final SectionTimeRepository sectionTimeRepository;
    private final CoursePrerequisiteRepository prerequisiteRepository;
    private final InterestAreaRepository interestAreaRepository;
    private final CourseInterestAreaRepository courseInterestAreaRepository;

    CsvImportPersistence(
            CourseRepository courseRepository,
            CourseSectionRepository sectionRepository,
            SectionTimeRepository sectionTimeRepository,
            CoursePrerequisiteRepository prerequisiteRepository,
            InterestAreaRepository interestAreaRepository,
            CourseInterestAreaRepository courseInterestAreaRepository) {
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.sectionTimeRepository = sectionTimeRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.interestAreaRepository = interestAreaRepository;
        this.courseInterestAreaRepository = courseInterestAreaRepository;
    }

    void save(
            List<CourseRow> courseRows,
            List<CourseSectionRow> sectionRows,
            List<SectionTimeRow> timeRows,
            List<CoursePrerequisiteRow> prerequisiteRows,
            List<CourseInterestAreaRow> interestAreaRows,
            String sectionFileName,
            String timeFileName,
            String prerequisiteFileName,
            String interestAreaFileName) {
        Map<String, Course> courses = new HashMap<>();
        for (CourseRow row : courseRows) {
            if (!interestAreaRepository.existsByName(row.mainArea())) {
                throw new CsvImportException(
                        "course.csv", row.rowNumber(), "존재하지 않는 mainArea입니다: " + row.mainArea());
            }
            Course course = courseRepository.findByCourseCode(row.courseCode())
                    .orElseGet(() -> courseRepository.save(new Course(
                            row.courseCode(), row.name(), row.credits(), row.courseType())));
            course.assignMainArea(row.mainArea());
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

        for (CoursePrerequisiteRow row : prerequisiteRows) {
            Course course = requireCourse(courses, row.courseCode(), prerequisiteFileName, row.rowNumber());
            Course prerequisite = requireCourse(
                    courses, row.prerequisiteCourseCode(), prerequisiteFileName, row.rowNumber());
            CoursePrerequisiteId id = new CoursePrerequisiteId(course.getId(), prerequisite.getId());
            if (!prerequisiteRepository.existsById(id)) {
                prerequisiteRepository.save(new CoursePrerequisite(course, prerequisite, row.relationType()));
            }
        }

        Map<String, InterestArea> interestAreas = new HashMap<>();
        for (CourseInterestAreaRow row : interestAreaRows) {
            Course course = requireCourse(courses, row.courseCode(), interestAreaFileName, row.rowNumber());
            InterestArea interestArea = interestAreas.computeIfAbsent(
                    row.interestAreaName(),
                    name -> interestAreaRepository.findByName(name).orElse(null));
            if (interestArea == null) {
                throw new CsvImportException(
                        interestAreaFileName,
                        row.rowNumber(),
                        "존재하지 않는 interestAreaName입니다: " + row.interestAreaName());
            }
            CourseInterestAreaId id = new CourseInterestAreaId(course.getId(), interestArea.getId());
            if (!courseInterestAreaRepository.existsById(id)) {
                courseInterestAreaRepository.save(new CourseInterestArea(course, interestArea));
            }
        }

    }

    private Course requireCourse(
            Map<String, Course> courses, String courseCode, String fileName, long rowNumber) {
        Course course = courses.computeIfAbsent(
                courseCode,
                code -> courseRepository.findByCourseCode(code).orElse(null));
        if (course == null) {
            throw new CsvImportException(
                    fileName, rowNumber, "존재하지 않는 courseCode입니다: " + courseCode);
        }
        return course;
    }
}
