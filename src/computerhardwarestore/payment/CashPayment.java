package computerhardwarestore.payment;

import computerhardwarestore.exception.PaymentException;
import java.util.List;
import java.util.Map;

/** Cash on delivery: nothing to enter. */
public class CashPayment implements PaymentMethod {
    @Override public String getName() { return "Cash"; }

    @Override public List<PaymentField> getFields() { return List.of(); }

    @Override public void validate(Map<String, String> details) throws PaymentException {
        // Nothing to validate.
    }

    @Override public String describe(Map<String, String> details) { return "Cash on delivery"; }
}
