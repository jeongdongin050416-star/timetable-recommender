package com.example.timetablerecommender.importer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;

abstract class AbstractCsvParser<T> {

    List<T> parse(Resource resource) {
        String fileName = resource.getFilename() == null ? resource.getDescription() : resource.getFilename();
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .get()
                     .parse(reader)) {
            validateHeaders(parser, fileName);
            List<T> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                long rowNumber = record.getRecordNumber() + 1;
                try {
                    rows.add(map(record, rowNumber, fileName));
                } catch (CsvImportException exception) {
                    throw exception;
                } catch (RuntimeException exception) {
                    throw new CsvImportException(fileName, rowNumber, exception.getMessage(), exception);
                }
            }
            return rows;
        } catch (CsvImportException exception) {
            throw exception;
        } catch (IOException | IllegalArgumentException exception) {
            throw new CsvImportException(fileName, 1, "CSV를 읽을 수 없습니다: " + exception.getMessage(), exception);
        }
    }

    private void validateHeaders(CSVParser parser, String fileName) {
        for (String header : requiredHeaders()) {
            if (!parser.getHeaderMap().containsKey(header)) {
                throw new CsvImportException(fileName, 1, "필수 헤더가 없습니다: " + header);
            }
        }
    }

    protected String required(CSVRecord record, String column, long rowNumber, String fileName) {
        String value = record.get(column).trim();
        if (value.isEmpty()) {
            throw new CsvImportException(fileName, rowNumber, column + " 값이 비어 있습니다");
        }
        return value;
    }

    protected abstract List<String> requiredHeaders();

    protected abstract T map(CSVRecord record, long rowNumber, String fileName);
}
