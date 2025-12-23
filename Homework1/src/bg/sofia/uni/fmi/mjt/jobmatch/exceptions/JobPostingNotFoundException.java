package bg.sofia.uni.fmi.mjt.jobmatch.exceptions;

public class JobPostingNotFoundException extends RuntimeException {
    public JobPostingNotFoundException(String message) {
        super(message);
    }

    public JobPostingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
