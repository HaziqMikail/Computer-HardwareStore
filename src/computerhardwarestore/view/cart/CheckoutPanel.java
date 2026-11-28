package computerhardwarestore.view.cart;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.controller.OrderController;
import computerhardwarestore.model.Order;
import computerhardwarestore.payment.PaymentField;
import computerhardwarestore.payment.PaymentMethod;
import computerhardwarestore.util.Formats;
import computerhardwarestore.view.common.Card;
import computerhardwarestore.view.common.Dialogs;
import computerhardwarestore.view.common.Navigator;
import computerhardwarestore.view.common.PageHeader;
import computerhardwarestore.view.common.Pages;
import computerhardwarestore.view.common.Refreshable;
import computerhardwarestore.view.common.StepBar;
import computerhardwarestore.view.common.Theme;
import computerhardwarestore.view.common.Ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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

/** Payment step (simulated). Method tiles on top, a form generated from the chosen method's fields below. */
public class CheckoutPanel extends JPanel implements Refreshable {
    private final AppContext ctx;
    private final Navigator nav;
    private final Consumer<Order> onPaid;
    private final List<PaymentMethod> methods;
    private PaymentMethod selected;

    private final JPanel tiles = new JPanel(new GridLayout(1, 0, 12, 0));
    private final JPanel fieldsPanel = new JPanel(new GridBagLayout());
    private final JPanel summary = Ui.transparent(null);
    private final Map<String, JComponent> inputs = new LinkedHashMap<>();

    public CheckoutPanel(AppContext ctx, Navigator nav, Consumer<Order> onPaid) {
        super(new BorderLayout());
        this.ctx = ctx;
        this.nav = nav;
        this.onPaid = onPaid;
        this.methods = ctx.payments.getMethods();
        this.selected = methods.get(0);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

        JPanel top = Ui.transparent(new BorderLayout());
        top.add(new StepBar(1), BorderLayout.NORTH);
        top.add(new PageHeader("Payment", "Choose how you would like to pay."), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel left = Ui.transparent(new BorderLayout(0, Theme.GAP));
        tiles.setOpaque(false);
        left.add(tiles, BorderLayout.NORTH);

        Card form = new Card(new BorderLayout(0, 10)).pad(18, 20, 18, 20);
        form.add(Ui.h2("Payment details"), BorderLayout.NORTH);
        fieldsPanel.setOpaque(false);
        form.add(fieldsPanel, BorderLayout.CENTER);
        JLabel note = Ui.muted("Demo store: no real payment is made. Details are only checked for format.");
        note.setForeground(Theme.WARNING);
        form.add(note, BorderLayout.SOUTH);
        left.add(form, BorderLayout.CENTER);
        add(left, BorderLayout.CENTER);

        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        JPanel right = Ui.transparent(new BorderLayout());
        right.setPreferredSize(new Dimension(316, 10));
        right.setBorder(BorderFactory.createEmptyBorder(0, Theme.GAP, 0, 0));
        right.add(summary, BorderLayout.NORTH);
        add(right, BorderLayout.EAST);
    }

    @Override
    public void refresh() {
        buildTiles();
        buildFields();
        buildSummary();
    }

    private void buildTiles() {
        tiles.removeAll();
        for (PaymentMethod m : methods) {
            boolean on = m == selected;
            Card tile = new Card(new BorderLayout(0, 2)).pad(14, 16, 14, 16)
                    .fill(on ? new Color(0xFFF6E0) : Color.WHITE).outline(on ? Theme.GOLD : Theme.LINE);
            JLabel name = Ui.bold(m.getName());
            JLabel hint = Ui.muted(hintFor(m));
            tile.add(name, BorderLayout.CENTER);
            tile.add(hint, BorderLayout.SOUTH);
            tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tile.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    selected = m;
                    buildTiles();
                    buildFields();
                }
            });
            tiles.add(tile);
        }
        tiles.revalidate();
        tiles.repaint();
    }

    private static String hintFor(PaymentMethod m) {
        return switch (m.getName()) {
            case "Cash" -> "Pay on delivery";
            case "Credit/Debit Card" -> "Visa, Mastercard";
            case "Online Banking" -> "Malaysian banks";
            default -> "";
        };
    }

    private void buildFields() {
        fieldsPanel.removeAll();
        inputs.clear();
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 12);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        for (PaymentField f : selected.getFields()) {
            g.gridy = row++;
            g.gridx = 0;
            g.weightx = 0;
            JLabel l = Ui.bold(f.label);
            l.setPreferredSize(new Dimension(190, 24));
            fieldsPanel.add(l, g);
            g.gridx = 1;
            g.weightx = 1;
            JComponent input;
            if (f.isChoice()) {
                input = new JComboBox<>(f.options.toArray(new String[0]));
            } else {
                JTextField t = new JTextField();
                ((AbstractDocument) t.getDocument()).setDocumentFilter(new LimitFilter(f.maxLength, f.digitsOnly));
                input = t;
            }
            inputs.put(f.key, input);
            fieldsPanel.add(input, g);
        }
        if (selected.getFields().isEmpty()) {
            g.gridy = 0;
            g.gridx = 0;
            g.gridwidth = 2;
            fieldsPanel.add(Ui.muted("No details needed. Pay the courier when your order arrives."), g);
        }
        fieldsPanel.revalidate();
        fieldsPanel.repaint();
    }

    private void buildSummary() {
        summary.removeAll();
        BigDecimal sub = ctx.cart.getCart().getSubtotal();
        BigDecimal total = sub.add(OrderController.DELIVERY_CHARGE);
        Card card = new Card(new BorderLayout(0, 14)).pad(18, 20, 18, 20);
        JPanel rows = Ui.transparent(null);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        { JLabel h = Ui.h2("Order summary"); h.setAlignmentX(Component.LEFT_ALIGNMENT); rows.add(h); }
        rows.add(Box.createVerticalStrut(12));
        for (var item : ctx.cart.getCart().getItems()) {
            rows.add(row(item.getQuantity() + " × " + item.getName(), Formats.money(item.getSubtotal()), false));
            rows.add(Box.createVerticalStrut(6));
        }
        rows.add(Box.createVerticalStrut(6));
        rows.add(row("Subtotal", Formats.money(sub), false));
        rows.add(Box.createVerticalStrut(6));
        rows.add(row("Delivery", Formats.money(OrderController.DELIVERY_CHARGE), false));
        rows.add(Box.createVerticalStrut(10));
        rows.add(row("Total", Formats.money(total), true));
        card.add(rows, BorderLayout.CENTER);

        JButton pay = Ui.accent("Pay " + Formats.money(total));
        JButton back = Ui.secondary("Back to cart");
        JPanel buttons = Ui.transparent(new GridLayout(2, 1, 0, 8));
        buttons.add(pay);
        buttons.add(back);
        card.add(buttons, BorderLayout.SOUTH);
        pay.addActionListener(e -> pay());
        back.addActionListener(e -> nav.show(Pages.CART));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.add(card);
        summary.revalidate();
        summary.repaint();
    }

    private static JPanel row(String label, String value, boolean big) {
        JPanel r = Ui.transparent(new BorderLayout(10, 0));
        JLabel l = big ? Ui.bold(label) : Ui.muted("<html>" + Formats.html(label) + "</html>");
        JLabel v = new JLabel(value);
        v.setFont(big ? Theme.H2 : Theme.BODY);
        r.add(l, BorderLayout.CENTER);
        r.add(v, BorderLayout.EAST);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        return r;
    }

    private void pay() {
        Map<String, String> details = new HashMap<>();
        for (Map.Entry<String, JComponent> e : inputs.entrySet()) {
            JComponent c = e.getValue();
            details.put(e.getKey(), c instanceof JComboBox<?> box
                    ? String.valueOf(box.getSelectedItem()) : ((JTextField) c).getText().trim());
        }
        try {
            // Never charge a stale price: re-check the cart against the live catalogue first.
            java.util.List<String> changes = ctx.cart.refreshPrices();
            if (!changes.isEmpty()) {
                nav.refreshHeader();
                Dialogs.warn(this, String.join("\n", changes) + "\n\nPlease review your order, then pay again.");
                if (ctx.cart.getCart().isEmpty()) {
                    nav.show(Pages.CART);
                } else {
                    buildSummary();
                }
                return;
            }
            Order order = ctx.orders.checkout(ctx.auth.getCurrentUser(), ctx.cart.getCart(), selected, details);
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
