package computerhardwarestore.view;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.controller.OrderController;
import computerhardwarestore.model.Order;
import computerhardwarestore.payment.PaymentField;
import computerhardwarestore.payment.PaymentMethod;
import computerhardwarestore.util.Formats;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** Checkout (simulated payment). The form is generated from the selected PaymentMethod's fields. */
public class CheckoutPanel extends JPanel implements Refreshable {
    private final AppContext ctx;
    private final Navigator nav;
    private final Consumer<Order> onPaid;
    private final JLabel summary = new JLabel(" ");
    private final JComboBox<PaymentMethod> methodBox = new JComboBox<>();
    private final JPanel fieldsPanel = new JPanel(new GridBagLayout());
    private final Map<String, JComponent> inputs = new LinkedHashMap<>();
    private final JLabel note = new JLabel(" ");

    public CheckoutPanel(AppContext ctx, Navigator nav, Consumer<Order> onPaid) {
        super(new BorderLayout(10, 10));
        this.ctx = ctx;
        this.nav = nav;
        this.onPaid = onPaid;
        setBackground(Theme.LIGHT);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        add(Theme.title("Checkout"), BorderLayout.NORTH);

        for (PaymentMethod m : ctx.payments.getMethods()) methodBox.addItem(m);
        methodBox.setRenderer((list, value, index, sel, focus) -> {
            JLabel l = new JLabel(value == null ? "" : value.getName());
            l.setOpaque(true);
            l.setFont(Theme.BODY_BOLD);
            l.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            l.setBackground(sel ? Theme.DARK : java.awt.Color.WHITE);
            l.setForeground(sel ? java.awt.Color.WHITE : java.awt.Color.BLACK);
            return l;
        });

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Theme.CREAM);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 3), BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        summary.setFont(Theme.BODY_BOLD);
        card.add(summary, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout(8, 8));
        mid.setOpaque(false);
        JPanel pick = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pick.setOpaque(false);
        pick.add(Theme.heading("Payment method:"));
        pick.add(methodBox);
        mid.add(pick, BorderLayout.NORTH);
        fieldsPanel.setOpaque(false);
        mid.add(fieldsPanel, BorderLayout.CENTER);
        note.setFont(Theme.BODY);
        note.setForeground(Theme.WARN);
        note.setText("Demo store: no real payment is made. Details are only checked for format.");
        mid.add(note, BorderLayout.SOUTH);
        card.add(mid, BorderLayout.CENTER);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.add(card);
        add(wrap, BorderLayout.CENTER);

        JPanel buttons = Theme.panel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton back = Theme.button("Back to cart");
        JButton pay = Theme.button("Pay now");
        buttons.add(back);
        buttons.add(pay);
        add(buttons, BorderLayout.SOUTH);

        back.addActionListener(e -> nav.show(MainFrame.CART));
        pay.addActionListener(e -> pay());
        methodBox.addActionListener(e -> buildFields());
        buildFields();
    }

    @Override
    public void refresh() {
        BigDecimal sub = ctx.cart.getCart().getSubtotal();
        BigDecimal total = sub.add(OrderController.DELIVERY_CHARGE);
        summary.setText("<html>Items: " + ctx.cart.getCart().getTotalQuantity() + " &nbsp;|&nbsp; Subtotal: "
                + Formats.money(sub) + " &nbsp;|&nbsp; Delivery: " + Formats.money(OrderController.DELIVERY_CHARGE)
                + " &nbsp;|&nbsp; <font color='#3e1f1b'>Total: " + Formats.money(total) + "</font></html>");
        buildFields();
    }

    private void buildFields() {
        fieldsPanel.removeAll();
        inputs.clear();
        PaymentMethod m = (PaymentMethod) methodBox.getSelectedItem();
        if (m == null) return;
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        int row = 0;
        for (PaymentField f : m.getFields()) {
            g.gridy = row++;
            g.gridx = 0;
            JLabel l = new JLabel(f.label + ":");
            l.setFont(Theme.BODY_BOLD);
            fieldsPanel.add(l, g);
            g.gridx = 1;
            JComponent input;
            if (f.isChoice()) {
                input = new JComboBox<>(f.options.toArray(new String[0]));
            } else {
                JTextField t = new JTextField(Math.max(4, Math.min(f.maxLength + 2, 20)));
                ((AbstractDocument) t.getDocument()).setDocumentFilter(new LimitFilter(f.maxLength, f.digitsOnly));
                input = t;
            }
            inputs.put(f.key, input);
            fieldsPanel.add(input, g);
        }
        if (m.getFields().isEmpty()) {
            g.gridy = 0;
            g.gridx = 0;
            fieldsPanel.add(new JLabel("No details needed - pay the courier when your order arrives."), g);
        }
        fieldsPanel.revalidate();
        fieldsPanel.repaint();
    }

    private void pay() {
        PaymentMethod m = (PaymentMethod) methodBox.getSelectedItem();
        Map<String, String> details = new HashMap<>();
        for (Map.Entry<String, JComponent> e : inputs.entrySet()) {
            JComponent c = e.getValue();
            details.put(e.getKey(), c instanceof JComboBox<?> box
                    ? String.valueOf(box.getSelectedItem()) : ((JTextField) c).getText().trim());
        }
        try {
            Order order = ctx.orders.checkout(ctx.auth.getCurrentUser(), ctx.cart.getCart(), m, details);
            buildFields(); // clear card details from the form
            nav.refreshHeader();
            onPaid.accept(order);
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    /** Limits length and (optionally) allows digits only, including for pasted text. */
    private static class LimitFilter extends DocumentFilter {
        private final int max;
        private final boolean digitsOnly;

        LimitFilter(int max, boolean digitsOnly) {
            this.max = max;
            this.digitsOnly = digitsOnly;
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            String t = text == null ? "" : text;
            if (digitsOnly && !t.matches("\\d*")) return;
            if (fb.getDocument().getLength() - length + t.length() > max) return;
            super.replace(fb, offset, length, t, attrs);
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            replace(fb, offset, 0, text, attr);
        }
    }
}
