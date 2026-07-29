package com.example.timetablerecommender.importer;

import java.time.DayOfWeek;
import java.time.LocalTime;

record CourseRow(String courseCode, String name, int credits, String courseType, long rowNumber) {
}

record CourseSectionRow(
        String sectionKey,
        String courseCode,
        int year,
        String semester,
        String sectionNumber,
        long rowNumber) {
}

record SectionTimeRow(
        String sectionKey,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        long rowNumber) {
}
