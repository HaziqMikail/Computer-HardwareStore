package computerhardwarestore.payment;

import computerhardwarestore.exception.PaymentException;
import java.util.List;
import java.util.Map;

/** Online banking. Format checks only (10-digit account number, bank chosen). */
public class OnlineBankingPayment implements PaymentMethod {
    @Override public String getName() { return "Online Banking"; }

    @Override public List<PaymentField> getFields() {
        return List.of(
                PaymentField.choice("bank", "Bank", List.of("CIMB", "Maybank", "Bank Islam", "RHB Bank", "Bank Rakyat")),
                PaymentField.digits("account", "Account number (10 digits)", 10));
    }

    @Override public void validate(Map<String, String> d) throws PaymentException {
        if (d.getOrDefault("bank", "").isBlank()) {
            throw new PaymentException("Please select a bank.");
        }
        if (!d.getOrDefault("account", "").matches("\\d{10}")) {
            throw new PaymentException("Please enter a valid 10-digit bank account number.");
        }
    }

    @Override public String describe(Map<String, String> d) {
        String acc = d.getOrDefault("account", "");
        String last4 = acc.length() >= 4 ? acc.substring(acc.length() - 4) : "";
        return d.getOrDefault("bank", "Bank") + " account ending " + last4;
    }
}
