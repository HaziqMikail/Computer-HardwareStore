package computerhardwarestore.exception;

/** Thrown when form input (registration, product form, ...) fails validation. */
public class ValidationException extends AppException {
    public ValidationException(String message) { super(message); }
}
