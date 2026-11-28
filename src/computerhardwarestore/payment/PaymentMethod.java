package computerhardwarestore.payment;

import computerhardwarestore.exception.PaymentException;
import java.util.List;
import java.util.Map;

/**
 * A payment method. This is a simulation: validation checks the format of the details only, nothing is charged.
 * To add a new method, implement this interface and register it in PaymentRegistry.
 */
public interface PaymentMethod {
    String getName();

    /** The input fields the checkout form should show for this method. */
    List<PaymentField> getFields();

    /** @param details field key -> value entered by the user */
    void validate(Map<String, String> details) throws PaymentException;

    /** Short, safe text for the receipt (never the full card/account number). */
    String describe(Map<String, String> details);
}
