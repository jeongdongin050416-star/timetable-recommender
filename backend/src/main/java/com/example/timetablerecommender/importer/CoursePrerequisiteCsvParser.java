package com.example.timetablerecommender.importer;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import com.example.timetablerecommender.domain.RelationType;

@Component
class CoursePrerequisiteCsvParser extends AbstractCsvParser<CoursePrerequisiteRow> {

    @Override
    protected List<String> requiredHeaders() {
        return List.of("courseCode", "prerequisiteCourseCode", "relationType");
    }

    @Override
    protected CoursePrerequisiteRow map(CSVRecord record, long rowNumber, String fileName) {
        String courseCode = required(record, "courseCode", rowNumber, fileName);
        String prerequisiteCourseCode = required(record, "prerequisiteCourseCode", rowNumber, fileName);
        if (courseCode.equals(prerequisiteCourseCode)) {
            throw new CsvImportException(fileName, rowNumber, "과목은 자기 자신의 선수 과목일 수 없습니다");
        }
        return new CoursePrerequisiteRow(
                courseCode,
                prerequisiteCourseCode,
                RelationType.valueOf(required(record, "relationType", rowNumber, fileName)),
                rowNumber);
    }
}
