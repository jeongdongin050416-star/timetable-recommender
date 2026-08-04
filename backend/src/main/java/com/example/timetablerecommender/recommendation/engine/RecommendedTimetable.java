package com.example.timetablerecommender.recommendation.engine;

import java.util.List;

public record RecommendedTimetable(int score, List<TimetableSelection> selections) {

    public RecommendedTimetable {
        selections = List.copyOf(selections);
    }
}
