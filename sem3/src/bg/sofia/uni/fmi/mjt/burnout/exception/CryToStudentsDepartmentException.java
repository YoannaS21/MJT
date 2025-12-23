package bg.sofia.uni.fmi.mjt.burnout.exception;

public class CryToStudentsDepartmentException extends RuntimeException {
    public CryToStudentsDepartmentException(String message) {
        super(message);
    }
    public CryToStudentsDepartmentException(String message, Throwable cause) {
        super(message , cause);
    }

}
