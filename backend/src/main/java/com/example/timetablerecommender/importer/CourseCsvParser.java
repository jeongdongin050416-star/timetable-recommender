package com.example.timetablerecommender.importer;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
class CourseCsvParser extends AbstractCsvParser<CourseRow> {

    @Override
    protected List<String> requiredHeaders() {
        return List.of("courseCode", "name", "credits", "courseType", "mainArea");
    }

    @Override
    protected CourseRow map(CSVRecord record, long rowNumber, String fileName) {
        int credits = Integer.parseInt(required(record, "credits", rowNumber, fileName));
        if (credits <= 0) {
            throw new CsvImportException(fileName, rowNumber, "credits는 양수여야 합니다");
        }
        return new CourseRow(
                required(record, "courseCode", rowNumber, fileName),
                required(record, "name", rowNumber, fileName),
                credits,
                required(record, "courseType", rowNumber, fileName),
                required(record, "mainArea", rowNumber, fileName),
                rowNumber);
    }
}
