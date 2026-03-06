package bg.sofia.uni.fmi.mjt.foodanalyzer.exception;

public class ExternalApiException extends FoodAnalyzerException {
    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
