package bg.sofia.uni.fmi.mjt.news.feed.exception;

public class NewsFeedClientException extends Exception {
    public NewsFeedClientException(String message) {
        super(message);
    }

    public NewsFeedClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
