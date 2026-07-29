package com.example.timetablerecommender.importer;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
class CourseSectionCsvParser extends AbstractCsvParser<CourseSectionRow> {

    @Override
    protected List<String> requiredHeaders() {
        return List.of("sectionKey", "courseCode", "year", "semester", "sectionNumber");
    }

    @Override
    protected CourseSectionRow map(CSVRecord record, long rowNumber, String fileName) {
        int year = Integer.parseInt(required(record, "year", rowNumber, fileName));
        if (year <= 0) {
            throw new CsvImportException(fileName, rowNumber, "year는 양수여야 합니다");
        }
        return new CourseSectionRow(
                required(record, "sectionKey", rowNumber, fileName),
                required(record, "courseCode", rowNumber, fileName),
                year,
                required(record, "semester", rowNumber, fileName),
                required(record, "sectionNumber", rowNumber, fileName),
                rowNumber);
    }
}
