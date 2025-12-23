package bg.sofia.uni.fmi.mjt.fittrack.comparators;

import bg.sofia.uni.fmi.mjt.fittrack.workout.Workout;

import java.util.Comparator;

public class WorkoutsByCaloriesComparator implements Comparator<Workout> {
    @Override
    public int compare(Workout a, Workout b) {
        return Integer.compare(b.getCaloriesBurned(), a.getCaloriesBurned());
    }
}
