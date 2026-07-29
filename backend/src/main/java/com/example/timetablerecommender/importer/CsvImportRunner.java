package com.example.timetablerecommender.importer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app.csv-import", name = "enabled", havingValue = "true")
class CsvImportRunner implements CommandLineRunner {

    private final CsvImportService importService;

    CsvImportRunner(CsvImportService importService) {
        this.importService = importService;
    }

    @Override
    public void run(String... args) {
        importService.importCsvFiles();
    }
}
