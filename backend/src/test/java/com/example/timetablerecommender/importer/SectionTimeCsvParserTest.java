package com.example.timetablerecommender.importer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

class SectionTimeCsvParserTest {

    private final SectionTimeCsvParser parser = new SectionTimeCsvParser();

    @Test
    void rejectsStartTimeThatIsNotBeforeEndTimeWithFileAndRow() {
        ByteArrayResource resource = new ByteArrayResource("""
                sectionKey,dayOfWeek,startTime,endTime,classroom
                S1,MONDAY,10:00,10:00,E1
                """.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "section_time.csv";
            }
        };

        assertThatThrownBy(() -> parser.parse(resource))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("section_time.csv")
                .hasMessageContaining("row 2")
                .hasMessageContaining("시작 시간은 종료 시간보다 빨라야 합니다");
    }
}
