package computerhardwarestore.exception;

/** Thrown when a quantity is empty, not a number, zero or negative. */
public class InvalidQuantityException extends AppException {
    public InvalidQuantityException(String message) { super(message); }
}
