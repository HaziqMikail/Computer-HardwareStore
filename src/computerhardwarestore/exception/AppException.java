package computerhardwarestore.exception;

/** Base class for all application-specific (checked) exceptions. Messages are safe to show to the user. */
public class AppException extends Exception {
    public AppException(String message) { super(message); }
    public AppException(String message, Throwable cause) { super(message, cause); }
}
