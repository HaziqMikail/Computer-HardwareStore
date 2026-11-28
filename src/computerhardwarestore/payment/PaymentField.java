package computerhardwarestore.payment;

import java.util.List;

/** Describes one input on the checkout form: free text/digits, or a choice from a list. */
public final class PaymentField {
    public final String key;
    public final String label;
    public final List<String> options; // null = text field
    public final int maxLength;        // text fields only
    public final boolean digitsOnly;

    private PaymentField(String key, String label, List<String> options, int maxLength, boolean digitsOnly) {
        this.key = key;
        this.label = label;
        this.options = options;
        this.maxLength = maxLength;
        this.digitsOnly = digitsOnly;
    }

    public static PaymentField choice(String key, String label, List<String> options) {
        return new PaymentField(key, label, options, 0, false);
    }

    public static PaymentField digits(String key, String label, int maxLength) {
        return new PaymentField(key, label, null, maxLength, true);
    }


    public boolean isChoice() { return options != null; }
}
