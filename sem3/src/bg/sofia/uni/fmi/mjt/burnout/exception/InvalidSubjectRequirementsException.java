package bg.sofia.uni.fmi.mjt.burnout.exception;

public class InvalidSubjectRequirementsException extends Exception {
    public InvalidSubjectRequirementsException(String message) {
        super(message);
    }
    public InvalidSubjectRequirementsException(String message, Throwable cause) {
        super(message , cause);
    }

}
