package bg.sofia.uni.fmi.mjt.burnout.exception;

public class DisappointmentException extends RuntimeException {
    public DisappointmentException(String message) {
        super(message);
    }
    public DisappointmentException(String message, Throwable cause) {
        super(message , cause);
    }
}
