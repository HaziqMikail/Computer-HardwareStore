package computerhardwarestore.exception;

/** Thrown when the username/password combination is wrong. */
public class InvalidLoginException extends AppException {
    public InvalidLoginException(String message) { super(message); }
}
