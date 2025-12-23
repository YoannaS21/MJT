package bg.sofia.uni.fmi.mjt.fittrack.workout.filter;

import bg.sofia.uni.fmi.mjt.fittrack.workout.Workout;

public class NameWorkoutFilter implements WorkoutFilter {
    private final String keyword;
    private final boolean caseSensitive;

    public NameWorkoutFilter(String keyword, boolean caseSensitive) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword is null or blank.");
        }
        this.keyword = keyword;
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean matches(Workout workout) {
        if (caseSensitive) {
            return workout.getName().contains(keyword);
        } else {
            return workout.getName().toLowerCase().contains(keyword.toLowerCase());
        }
    }
}
