package com.example.timetablerecommender.importer;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
class CourseInterestAreaCsvParser extends AbstractCsvParser<CourseInterestAreaRow> {

    @Override
    protected List<String> requiredHeaders() {
        return List.of("courseCode", "interestAreaName");
    }

    @Override
    protected CourseInterestAreaRow map(CSVRecord record, long rowNumber, String fileName) {
        return new CourseInterestAreaRow(
                required(record, "courseCode", rowNumber, fileName),
                required(record, "interestAreaName", rowNumber, fileName),
                rowNumber);
    }
}
