package bg.sofia.uni.fmi.mjt.fittrack.workout.filter;

import bg.sofia.uni.fmi.mjt.fittrack.workout.Workout;

public class CaloriesWorkoutFilter implements WorkoutFilter {
    private final int min;
    private final int max;

    public CaloriesWorkoutFilter(int min, int max) {
        if (max < 0 || min < 0 || min > max) {
            throw new IllegalArgumentException("Invalid min/max.");
        }
        this.max = max;
        this.min = min;
    }

    @Override
    public boolean matches(Workout workout) {
        return (min <= workout.getCaloriesBurned() && max >= workout.getCaloriesBurned());
    }
}
