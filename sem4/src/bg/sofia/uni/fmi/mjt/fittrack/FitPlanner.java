package bg.sofia.uni.fmi.mjt.fittrack;

import bg.sofia.uni.fmi.mjt.fittrack.comparators.WorkoutsByCaloriesComparator;
import bg.sofia.uni.fmi.mjt.fittrack.comparators.WorkoutsByDifficultyComparator;
import bg.sofia.uni.fmi.mjt.fittrack.comparators.WorkoutsCaloriesDifficultyComparator;
import bg.sofia.uni.fmi.mjt.fittrack.exception.OptimalPlanImpossibleException;
import bg.sofia.uni.fmi.mjt.fittrack.workout.Workout;
import bg.sofia.uni.fmi.mjt.fittrack.workout.WorkoutType;
import bg.sofia.uni.fmi.mjt.fittrack.workout.filter.WorkoutFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FitPlanner implements FitPlannerAPI {
    List<Workout> availableWorkouts;

    public FitPlanner(Collection<Workout> availableWorkouts) {
        if (availableWorkouts == null) {
            throw new IllegalArgumentException("Available workouts is null.");
        }
        this.availableWorkouts = new ArrayList<>(availableWorkouts);
    }

    /**
     * Returns a list of workouts that match all provided filters.
     *
     * @param filters a list of filters to be applied.
     * @return a list of workouts that satisfy all filters.
     * @throws IllegalArgumentException if filters is null.
     */
    @Override
    public List<Workout> findWorkoutsByFilters(List<WorkoutFilter> filters) {
        if (filters == null) {
            throw new IllegalArgumentException("Filters is null.");
        }

        if (availableWorkouts.isEmpty()) {
            return Collections.emptyList();
        }

        List<Workout> workoutsPassedFilters = new ArrayList<>();
        for (Workout workout : availableWorkouts) {
            boolean passAllFilters = true;
            for (WorkoutFilter filter : filters) {
                if (!filter.matches(workout)) {
                    passAllFilters = false;
                    break;
                }
            }
            if (passAllFilters) {
                workoutsPassedFilters.add(workout);
            }
        }
        return workoutsPassedFilters;
    }

    /**
     * Generates an optimal weekly workout plan that maximizes burned calories
     * while fitting within the specified total time limit.
     *
     * @param totalMinutes total available time (in minutes) for workouts during the week
     * @return a list of optimally selected workouts, sorted by calories, then by difficulty, in descending order.
     * Returns an empty list if totalMinutes is 0.
     * @throws OptimalPlanImpossibleException if a valid plan cannot be generated
     * (e.g., all workouts exceed the time limit)
     * @throws IllegalArgumentException       if totalMinutes is negative
     */

    private List<Workout> extractOptimalPlan(int[][] dp, int totalMinutes) {
        List<Workout> selected = new ArrayList<>();
        int t = totalMinutes;

        for (int i = availableWorkouts.size(); i > 0; i--) {
            if (dp[i][t] != dp[i - 1][t]) {
                Workout w = availableWorkouts.get(i - 1);
                selected.add(w);
                t -= w.getDuration();
            }
        }
        return selected;
    }

    private int[][] buildTable(int totalMinutes) {
        int n = availableWorkouts.size();
        int[][] dp = new int[n + 1][totalMinutes + 1];

        for (int i = 1; i <= availableWorkouts.size(); i++) {
            Workout workout = availableWorkouts.get(i - 1);
            int duration = workout.getDuration();
            int calories = workout.getCaloriesBurned();

            for (int t = 0; t <= totalMinutes; t++) {
                if (duration > t) {
                    dp[i][t] = dp[i - 1][t];
                } else {
                    dp[i][t] = Math.max(dp[i - 1][t], dp[i - 1][t - duration] + calories);
                }
            }
        }
        return dp;
    }

    @Override
    public List<Workout> generateOptimalWeeklyPlan(int totalMinutes) throws OptimalPlanImpossibleException {
        if (totalMinutes < 0) {
            throw new IllegalArgumentException("Negative minutes.");
        }

        if (totalMinutes == 0 || availableWorkouts.isEmpty()) {
            return Collections.emptyList();
        }

        int[][] dp = buildTable(totalMinutes);

        if (dp[availableWorkouts.size()][totalMinutes] == 0) {
            throw new OptimalPlanImpossibleException("Couldn't fit any workout within the time limit.");
        }

        List<Workout> selected = extractOptimalPlan(dp, totalMinutes);
        selected.sort(new WorkoutsCaloriesDifficultyComparator());
        return Collections.unmodifiableList(selected);
    }

    /**
     * Groups all available workouts by type.
     *
     * @return an unmodifiable Map where the key is WorkoutType and the value is a list of workouts of that type.
     */
    @Override
    public Map<WorkoutType, List<Workout>> getWorkoutsGroupedByType() {
        if (availableWorkouts.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<WorkoutType, List<Workout>> groupedByType = new HashMap<>();
        for (Workout workout : availableWorkouts) {
            WorkoutType type = workout.getType();
            if (!groupedByType.containsKey(type)) {
                groupedByType.put(type, new ArrayList<>());
            }
            groupedByType.get(type).add(workout);
        }

        return Collections.unmodifiableMap(groupedByType);
    }

    /**
     * Returns a list of all workouts, sorted by burned calories in descending order.
     *
     * @return an unmodifiable list of workouts sorted by calories in descending order.
     */
    @Override
    public List<Workout> getWorkoutsSortedByCalories() {
        if (availableWorkouts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Workout> workoutSortedByCalories = new ArrayList<>(availableWorkouts);
        workoutSortedByCalories.sort(new WorkoutsByCaloriesComparator());
        return Collections.unmodifiableList(workoutSortedByCalories);
    }

    /**
     * Returns a list of all workouts, sorted by difficulty in ascending order.
     *
     * @return an unmodifiable list of workouts sorted by difficulty in ascending order.
     */
    @Override
    public List<Workout> getWorkoutsSortedByDifficulty() {
        if (availableWorkouts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Workout> workoutsSortedByDifficulty = new ArrayList<>(availableWorkouts);
        workoutsSortedByDifficulty.sort(new WorkoutsByDifficultyComparator());
        return Collections.unmodifiableList(workoutsSortedByDifficulty);
    }

    /**
     * Returns an unmodifiable set of all available workouts.
     *
     * @return an unmodifiable Set containing all workouts.
     */
    @Override
    public Set<Workout> getUnmodifiableWorkoutSet() {
        if (availableWorkouts.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Workout> unmodifiableWorkoutSet = new HashSet<>(availableWorkouts);
        return Collections.unmodifiableSet(unmodifiableWorkoutSet);
    }
}
