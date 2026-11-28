package computerhardwarestore.view.cart;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.controller.OrderController;
import computerhardwarestore.model.CartItem;
import computerhardwarestore.util.Formats;
import computerhardwarestore.view.common.Card;
import computerhardwarestore.view.common.Dialogs;
import computerhardwarestore.view.common.ImageLoader;
import computerhardwarestore.view.common.Navigator;
import computerhardwarestore.view.common.PageHeader;
import computerhardwarestore.view.common.Pages;
import computerhardwarestore.view.common.Refreshable;
import computerhardwarestore.view.common.StepBar;
import computerhardwarestore.view.common.Theme;
import computerhardwarestore.view.common.Ui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** The cart: line items with +/- quantity, remove, and an order summary. */
public class CartPanel extends JPanel implements Refreshable {
    private final AppContext ctx;
    private final Navigator nav;
    private final JPanel items = new JPanel();
    private final JPanel summary = Ui.transparent(null);
    private final JPanel content = Ui.transparent(new BorderLayout(Theme.GAP, 0));
    private final JPanel emptyState = new JPanel(new GridBagLayout());

    public CartPanel(AppContext ctx, Navigator nav) {
        super(new BorderLayout());
        this.ctx = ctx;
        this.nav = nav;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

        JPanel top = Ui.transparent(new BorderLayout());
        top.add(new StepBar(0), BorderLayout.NORTH);
        top.add(new PageHeader("Your cart", "Review your items before paying."), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.setOpaque(false);
        content.add(Ui.scroll(items), BorderLayout.CENTER);
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        JPanel right = Ui.transparent(new BorderLayout());
        right.setPreferredSize(new Dimension(300, 10));
        right.add(summary, BorderLayout.NORTH);
        content.add(right, BorderLayout.EAST);

        emptyState.setOpaque(false);
        JPanel e = Ui.transparent(null);
        e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel("🛒", SwingConstants.CENTER);
        icon.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 56));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel t = Ui.h2("Your cart is empty");
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel m = Ui.muted("Add something from the shop to get started.");
        m.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton shop = Ui.accent("Browse products");
        shop.setAlignmentX(Component.CENTER_ALIGNMENT);
        shop.addActionListener(ev -> nav.show(Pages.SHOP));
        e.add(icon);
        e.add(Box.createVerticalStrut(8));
        e.add(t);
        e.add(Box.createVerticalStrut(4));
        e.add(m);
        e.add(Box.createVerticalStrut(18));
        e.add(shop);
        emptyState.add(e, new GridBagConstraints());
    }

    @Override
    public void refresh() {
        try {
            java.util.List<String> changes = ctx.cart.refreshPrices();
            if (!changes.isEmpty()) {
                Dialogs.warn(this, String.join("\n", changes));
            }
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
        remove(content);
        remove(emptyState);
        if (ctx.cart.getCart().isEmpty()) {
            add(emptyState, BorderLayout.CENTER);
        } else {
            buildItems();
            buildSummary();
            add(content, BorderLayout.CENTER);
        }
        nav.refreshHeader();
        revalidate();
        repaint();
    }

    private void buildItems() {
        items.removeAll();
        for (CartItem item : ctx.cart.getCart().getItems()) {
            items.add(line(item));
            items.add(Box.createVerticalStrut(10));
        }
    }

    private JPanel line(CartItem item) {
        Card card = new Card(new BorderLayout(14, 0)).pad(10, 10, 10, 16);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel img = new JLabel(ImageLoader.load(item.getImagePath(), 90, 80), SwingConstants.CENTER);
        img.setOpaque(true);
        img.setBackground(new java.awt.Color(0xFAF7F2));
        img.setPreferredSize(new Dimension(96, 86));
        card.add(img, BorderLayout.WEST);

        JPanel mid = Ui.transparent(new GridBagLayout());
        JPanel text = Ui.transparent(null);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Ui.bold(item.getName()));
        text.add(Box.createVerticalStrut(2));
        text.add(Ui.muted(Formats.money(item.getUnitPrice()) + " each"));
        mid.add(text, new GridBagConstraints());
        JPanel midWrap = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 0, 0));
        midWrap.add(mid);
        card.add(midWrap, BorderLayout.CENTER);

        JPanel right = Ui.transparent(new GridBagLayout());
        JPanel col = Ui.transparent(null);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JPanel stepper = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton minus = stepButton("−");
        JButton plus = stepButton("+");
        JLabel qty = Ui.bold(String.valueOf(item.getQuantity()));
        qty.setHorizontalAlignment(SwingConstants.CENTER);
        qty.setPreferredSize(new Dimension(30, 28));
        stepper.add(minus);
        stepper.add(qty);
        stepper.add(plus);
        stepper.setAlignmentX(Component.RIGHT_ALIGNMENT);
        col.add(stepper);
        col.add(Box.createVerticalStrut(6));

        JPanel bottom = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JLabel sub = new JLabel(Formats.money(item.getSubtotal()));
        sub.setFont(Theme.H2);
        JButton remove = Ui.link("Remove");
        remove.setForeground(Theme.DANGER);
        bottom.add(sub);
        bottom.add(remove);
        bottom.setAlignmentX(Component.RIGHT_ALIGNMENT);
        col.add(bottom);
        right.add(col, new GridBagConstraints());
        card.add(right, BorderLayout.EAST);

        minus.setEnabled(item.getQuantity() > 1);
        minus.addActionListener(e -> change(item, item.getQuantity() - 1));
        plus.addActionListener(e -> change(item, item.getQuantity() + 1));
        remove.addActionListener(e -> {
            if (Dialogs.confirm(this, "Remove \"" + item.getName() + "\" from your cart?")) {
                ctx.cart.remove(item.getProductId());
                refresh();
            }
        });
        return card;
    }

    private void change(CartItem item, int quantity) {
        try {
            ctx.cart.updateQuantity(item.getProductId(), String.valueOf(quantity));
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
        refresh();
    }

    private static JButton stepButton(String text) {
        JButton b = Ui.secondary(text);
        b.putClientProperty("FlatLaf.style", "arc: 10; margin: 0,0,0,0; hoverBackground: #F3EEE6");
        b.setPreferredSize(new Dimension(34, 30));
        return b;
    }

    private void buildSummary() {
        summary.removeAll();
        BigDecimal sub = ctx.cart.getCart().getSubtotal();
        BigDecimal total = sub.add(OrderController.DELIVERY_CHARGE);

        Card card = new Card(new BorderLayout(0, 12)).pad(18, 20, 18, 20);
        JPanel rows = Ui.transparent(null);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        { JLabel h = Ui.h2("Order summary"); h.setAlignmentX(Component.LEFT_ALIGNMENT); rows.add(h); }
        rows.add(Box.createVerticalStrut(14));
        rows.add(summaryRow("Items (" + ctx.cart.getCart().getTotalQuantity() + ")", Formats.money(sub), false));
        rows.add(Box.createVerticalStrut(8));
        rows.add(summaryRow("Delivery", Formats.money(OrderController.DELIVERY_CHARGE), false));
        rows.add(Box.createVerticalStrut(12));
        rows.add(summaryRow("Total", Formats.money(total), true));
        card.add(rows, BorderLayout.CENTER);

        JButton checkout = Ui.accent("Proceed to payment");
        checkout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JButton more = Ui.secondary("Continue shopping");
        JPanel buttons = Ui.transparent(new java.awt.GridLayout(2, 1, 0, 8));
        buttons.add(checkout);
        buttons.add(more);
        card.add(buttons, BorderLayout.SOUTH);
        checkout.addActionListener(e -> nav.show(Pages.CHECKOUT));
        more.addActionListener(e -> nav.show(Pages.SHOP));

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.add(card);
    }

    private static JPanel summaryRow(String label, String value, boolean big) {
        JPanel r = Ui.transparent(new BorderLayout());
        JLabel l = big ? Ui.bold(label) : Ui.muted(label);
        JLabel v = new JLabel(value);
        v.setFont(big ? Theme.H2 : Theme.BODY);
        r.add(l, BorderLayout.WEST);
        r.add(v, BorderLayout.EAST);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        return r;
    }
}
