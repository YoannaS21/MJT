package bg.sofia.uni.fmi.mjt.fittrack.exception;

public class OptimalPlanImpossibleException extends Exception {
    public OptimalPlanImpossibleException(String message) {
        super(message);
    }

    public OptimalPlanImpossibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
