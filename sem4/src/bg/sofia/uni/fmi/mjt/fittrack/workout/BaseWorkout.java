package bg.sofia.uni.fmi.mjt.fittrack.workout;

import bg.sofia.uni.fmi.mjt.fittrack.exception.InvalidWorkoutException;

public abstract class BaseWorkout {
    protected String name;
    protected int duration;
    protected int caloriesBurned;
    protected int difficulty;
    private final int minDifficulty = 1;
    private final int maxDifficulty = 5;

    protected BaseWorkout(String name, int duration, int caloriesBurned, int difficulty) {
        validate(name, duration, caloriesBurned, difficulty);
        this.name = name;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.difficulty = difficulty;
    }

    private void validate(String name, int duration, int caloriesBurned, int difficulty) {
        if (name == null || name.isBlank())
            throw new InvalidWorkoutException("Name cannot be null or empty.");
        if (duration <= 0)
            throw new InvalidWorkoutException("Duration must be positive.");
        if (caloriesBurned <= 0)
            throw new InvalidWorkoutException("Calories must be positive.");
        if (difficulty < minDifficulty || difficulty > maxDifficulty)
            throw new InvalidWorkoutException("Difficulty must be between 1 and 5.");
    }

}
