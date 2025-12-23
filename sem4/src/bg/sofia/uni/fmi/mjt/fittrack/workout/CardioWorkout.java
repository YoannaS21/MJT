package bg.sofia.uni.fmi.mjt.fittrack.workout;

public final class CardioWorkout extends BaseWorkout implements Workout {
    public CardioWorkout(String name, int duration, int caloriesBurned, int difficulty) {
        super(name, duration, caloriesBurned, difficulty);
    }

    @Override
    public String getName() {
        return super.name;
    }

    @Override
    public int getDuration() {
        return super.duration;
    }

    @Override
    public int getCaloriesBurned() {
        return super.caloriesBurned;
    }

    @Override
    public int getDifficulty() {
        return super.difficulty;
    }

    @Override
    public WorkoutType getType() {
        return WorkoutType.CARDIO;
    }
}
