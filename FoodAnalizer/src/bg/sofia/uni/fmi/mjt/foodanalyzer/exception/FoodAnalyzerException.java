package bg.sofia.uni.fmi.mjt.foodanalyzer.exception;

public class FoodAnalyzerException extends Exception {
    public FoodAnalyzerException(String message) {
        super(message);
    }

    public FoodAnalyzerException(String message, Throwable cause) {
        super(message, cause);
    }
}
