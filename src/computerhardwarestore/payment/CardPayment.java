package computerhardwarestore.payment;

import computerhardwarestore.exception.PaymentException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/** Credit/debit card. Format checks only (16 digits, expiry not in the past, 3-digit CVV). */
public class CardPayment implements PaymentMethod {
    @Override public String getName() { return "Credit/Debit Card"; }

    @Override public List<PaymentField> getFields() {
        return List.of(
                PaymentField.choice("type", "Card type", List.of("Credit", "Debit")),
                PaymentField.digits("number", "Card number (16 digits)", 16),
                PaymentField.digits("expMonth", "Expiry month (MM)", 2),
                PaymentField.digits("expYear", "Expiry year (YY)", 2),
                PaymentField.digits("cvv", "CVV (3 digits)", 3));
    }

    @Override public void validate(Map<String, String> d) throws PaymentException {
        String number = d.getOrDefault("number", "");
        if (!number.matches("\\d{16}")) {
            throw new PaymentException("Please enter a valid 16-digit card number.");
        }
        int month;
        int year;
        try {
            month = Integer.parseInt(d.getOrDefault("expMonth", ""));
            year = 2000 + Integer.parseInt(d.getOrDefault("expYear", ""));
        } catch (NumberFormatException e) {
            throw new PaymentException("Please enter the card expiry date as MM and YY.");
        }
        if (month < 1 || month > 12) {
            throw new PaymentException("Expiry month must be between 01 and 12.");
        }
        if (YearMonth.of(year, month).isBefore(YearMonth.now())) {
            throw new PaymentException("This card has expired.");
        }
        if (!d.getOrDefault("cvv", "").matches("\\d{3}")) {
            throw new PaymentException("CVV must be exactly 3 digits.");
        }
    }

    @Override public String describe(Map<String, String> d) {
        String number = d.getOrDefault("number", "");
        String last4 = number.length() >= 4 ? number.substring(number.length() - 4) : "";
        return d.getOrDefault("type", "Card") + " card ending " + last4;
    }
}
