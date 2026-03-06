package bg.sofia.uni.fmi.mjt.foodanalyzer.exception;

public class FoodNotFoundException extends FoodAnalyzerException {
    public FoodNotFoundException(String message) {
        super(message);
    }

    public FoodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}