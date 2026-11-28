package computerhardwarestore.exception;

/** Thrown when payment details are missing or malformed. */
public class PaymentException extends AppException {
    public PaymentException(String message) { super(message); }
}
