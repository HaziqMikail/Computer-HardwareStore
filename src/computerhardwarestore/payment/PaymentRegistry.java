package computerhardwarestore.payment;

import java.util.ArrayList;
import java.util.List;

/** The list of payment methods offered at checkout. Register a new PaymentMethod here to offer it. */
public final class PaymentRegistry {
    private final List<PaymentMethod> methods = new ArrayList<>();

    public PaymentRegistry() {
        register(new CashPayment());
        register(new CardPayment());
        register(new OnlineBankingPayment());
    }

    public void register(PaymentMethod method) { methods.add(method); }

    public List<PaymentMethod> getMethods() { return List.copyOf(methods); }
}
