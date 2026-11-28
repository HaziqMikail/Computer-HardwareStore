package computerhardwarestore.exception;

/** Thrown when the requested quantity exceeds the available stock. */
public class OutOfStockException extends AppException {
    public OutOfStockException(String message) { super(message); }
}
