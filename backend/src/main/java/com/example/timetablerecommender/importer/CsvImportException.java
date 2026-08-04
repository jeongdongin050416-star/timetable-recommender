package com.example.timetablerecommender.importer;

public class CsvImportException extends RuntimeException {

    private final String fileName;
    private final long rowNumber;

    public CsvImportException(String fileName, long rowNumber, String message) {
        super(fileName + " row " + rowNumber + ": " + message);
        this.fileName = fileName;
        this.rowNumber = rowNumber;
    }

    public CsvImportException(String fileName, long rowNumber, String message, Throwable cause) {
        super(fileName + " row " + rowNumber + ": " + message, cause);
        this.fileName = fileName;
        this.rowNumber = rowNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public long getRowNumber() {
        return rowNumber;
    }
}
