package bg.sofia.uni.fmi.mjt.fittrack.comparators;

import bg.sofia.uni.fmi.mjt.fittrack.workout.Workout;

import java.util.Comparator;

public class WorkoutsCaloriesDifficultyComparator implements Comparator<Workout> {
    @Override
    public int compare(Workout a, Workout b) {
        int compareCalories = Integer.compare(b.getCaloriesBurned(), a.getCaloriesBurned());
        if (compareCalories != 0) {
            return compareCalories;
        } else {
            return Integer.compare(b.getDifficulty(), a.getDifficulty());
        }
    }
}
