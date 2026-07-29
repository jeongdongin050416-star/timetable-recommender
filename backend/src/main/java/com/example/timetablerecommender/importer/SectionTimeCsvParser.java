package com.example.timetablerecommender.importer;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
class SectionTimeCsvParser extends AbstractCsvParser<SectionTimeRow> {

    @Override
    protected List<String> requiredHeaders() {
        return List.of("sectionKey", "dayOfWeek", "startTime", "endTime");
    }

    @Override
    protected SectionTimeRow map(CSVRecord record, long rowNumber, String fileName) {
        DayOfWeek day = DayOfWeek.valueOf(required(record, "dayOfWeek", rowNumber, fileName));
        LocalTime start = LocalTime.parse(required(record, "startTime", rowNumber, fileName));
        LocalTime end = LocalTime.parse(required(record, "endTime", rowNumber, fileName));
        if (!start.isBefore(end)) {
            throw new CsvImportException(fileName, rowNumber, "시작 시간은 종료 시간보다 빨라야 합니다");
        }
        return new SectionTimeRow(
                required(record, "sectionKey", rowNumber, fileName), day, start, end, rowNumber);
    }
}
