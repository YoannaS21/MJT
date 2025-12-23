package bg.sofia.uni.fmi.mjt.fittrack.workout.filter;

import bg.sofia.uni.fmi.mjt.fittrack.workout.Workout;
import bg.sofia.uni.fmi.mjt.fittrack.workout.WorkoutType;

public class TypeWorkoutFilter implements WorkoutFilter {
    private final WorkoutType type;

    public TypeWorkoutFilter(WorkoutType type) {
        if (type == null) {
            throw new IllegalArgumentException("Type is null.");
        }
        this.type = type;
    }

    @Override
    public boolean matches(Workout workout) {
        return workout.getType() == type;
    }
}
