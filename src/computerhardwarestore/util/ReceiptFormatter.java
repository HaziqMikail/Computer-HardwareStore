package computerhardwarestore.util;

import computerhardwarestore.model.Order;
import computerhardwarestore.model.OrderItem;

/** Builds the plain-text receipt from a saved order. The same text is shown, saved to a file and printed. */
public final class ReceiptFormatter {
    private static final int WIDTH = 62;

    private ReceiptFormatter() {}

    public static String format(Order o) {
        StringBuilder sb = new StringBuilder();
        String line = "=".repeat(WIDTH);
        String thin = "-".repeat(WIDTH);
        sb.append(line).append('\n');
        sb.append(center("COMPUTER & HARDWARE STORE")).append('\n');
        sb.append(center("OFFICIAL RECEIPT")).append('\n');
        sb.append(line).append('\n');
        sb.append(String.format("Order ID : #%06d%n", o.getId()));
        sb.append(String.format("Date     : %s%n", Formats.dateTime(o.getOrderDate())));
        sb.append(String.format("Customer : %s%n", o.getCustomerName()));
        sb.append(String.format("Payment  : %s (%s)%n", o.getPaymentMethod(), o.getPaymentDetail()));
        sb.append(thin).append('\n');
        sb.append(String.format("%-30s %4s %12s %12s%n", "Item", "Qty", "Unit", "Total"));
        sb.append(thin).append('\n');
        for (OrderItem i : o.getItems()) {
            String name = i.getProductName();
            if (name.length() > 30) name = name.substring(0, 27) + "...";
            sb.append(String.format("%-30s %4d %12s %12s%n", name, i.getQuantity(),
                    String.format("%,.2f", i.getUnitPrice()), String.format("%,.2f", i.getLineTotal())));
        }
        sb.append(thin).append('\n');
        sb.append(String.format("%-47s %14s%n", "Subtotal:", Formats.money(o.getSubtotal())));
        sb.append(String.format("%-47s %14s%n", "Delivery charge:", Formats.money(o.getDeliveryCharge())));
        sb.append(String.format("%-47s %14s%n", "TOTAL PAID:", Formats.money(o.getTotal())));
        sb.append(line).append('\n');
        sb.append(center("Thank you for your purchase!")).append('\n');
        sb.append(center("Your items will arrive in 2-3 working days.")).append('\n');
        sb.append(line).append('\n');
        return sb.toString();
    }

    private static String center(String text) {
        int pad = Math.max(0, (WIDTH - text.length()) / 2);
        return " ".repeat(pad) + text;
    }
}
