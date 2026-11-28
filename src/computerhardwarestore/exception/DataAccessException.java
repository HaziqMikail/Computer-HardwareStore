package computerhardwarestore.exception;

/** Thrown by the data layer when a database operation fails. The cause holds the original SQLException. */
public class DataAccessException extends AppException {
    public DataAccessException(String message, Throwable cause) { super(message, cause); }
    public DataAccessException(String message) { super(message); }
}
