package computerhardwarestore.view;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.CartItem;
import computerhardwarestore.util.Formats;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** The cart: change quantities, remove lines, go to checkout. */
public class CartPanel extends JPanel implements Refreshable {
    private final AppContext ctx;
    private final Navigator nav;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Item", "Unit price", "Qty", "Subtotal"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel total = new JLabel(" ", SwingConstants.RIGHT);
    private final JLabel empty = new JLabel("Your cart is empty.", SwingConstants.CENTER);
    private final JButton update = Theme.button("Change quantity");
    private final JButton remove = Theme.button("Remove selected");
    private final JButton checkout = Theme.button("Checkout");

    public CartPanel(AppContext ctx, Navigator nav) {
        super(new BorderLayout(10, 10));
        this.ctx = ctx;
        this.nav = nav;
        setBackground(Theme.LIGHT);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        add(Theme.title("Your Cart"), BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        empty.setFont(Theme.HEADING);
        total.setFont(Theme.HEADING);
        total.setForeground(Theme.DARK);

        JPanel south = Theme.panel(new BorderLayout(6, 6));
        south.add(total, BorderLayout.NORTH);
        JPanel buttons = Theme.panel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton back = Theme.button("Continue shopping");
        buttons.add(back);
        buttons.add(update);
        buttons.add(remove);
        buttons.add(checkout);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        back.addActionListener(e -> nav.show(MainFrame.SHOP));
        update.addActionListener(e -> changeQuantity());
        remove.addActionListener(e -> removeSelected());
        checkout.addActionListener(e -> nav.show(MainFrame.CHECKOUT));
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        for (CartItem i : ctx.cart.getCart().getItems()) {
            model.addRow(new Object[]{i.getName(), Formats.money(i.getUnitPrice()), i.getQuantity(),
                    Formats.money(i.getSubtotal())});
        }
        boolean has = !ctx.cart.getCart().isEmpty();
        update.setEnabled(has);
        remove.setEnabled(has);
        checkout.setEnabled(has);
        BigDecimal sub = ctx.cart.getCart().getSubtotal();
        total.setText(has ? "Items total: " + Formats.money(sub) + "   (delivery fee added at checkout)"
                : "Your cart is empty - add something from the shop.");
        nav.refreshHeader();
    }

    private CartItem selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.warn(this, "Please select an item in the cart first.");
            return null;
        }
        return ctx.cart.getCart().getItems().get(row);
    }

    private void changeQuantity() {
        CartItem item = selected();
        if (item == null) return;
        Object input = JOptionPane.showInputDialog(this, "New quantity for \"" + item.getName() + "\":",
                "Change quantity", JOptionPane.QUESTION_MESSAGE, null, null, String.valueOf(item.getQuantity()));
        if (input == null) return; // cancelled
        try {
            ctx.cart.updateQuantity(item.getProductId(), input.toString());
            refresh();
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private void removeSelected() {
        CartItem item = selected();
        if (item == null) return;
        if (Dialogs.confirm(this, "Remove \"" + item.getName() + "\" from your cart?")) {
            ctx.cart.remove(item.getProductId());
            refresh();
        }
    }
}
