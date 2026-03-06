package bg.sofia.uni.fmi.mjt.news.feed.exception;

public class InvalidApiKeyException extends NewsFeedClientException {
    public InvalidApiKeyException(String message) {
        super(message);
    }

    public InvalidApiKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
