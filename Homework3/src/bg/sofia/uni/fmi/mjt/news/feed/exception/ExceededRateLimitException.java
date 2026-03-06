package bg.sofia.uni.fmi.mjt.news.feed.exception;

public class ExceededRateLimitException extends NewsFeedClientException {
    public ExceededRateLimitException(String message) {
        super(message);
    }

    public ExceededRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
