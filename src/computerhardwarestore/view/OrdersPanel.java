package computerhardwarestore.view;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.Order;
import computerhardwarestore.util.Formats;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Order history. Customers see their own orders; in admin mode all customers' orders plus a sales summary. */
public class OrdersPanel extends JPanel implements Refreshable {
    private final AppContext ctx;
    private final boolean adminMode;
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel summary = new JLabel(" ");
    private List<Order> orders = new ArrayList<>();

    public OrdersPanel(AppContext ctx, boolean adminMode) {
        super(new BorderLayout(10, 10));
        this.ctx = ctx;
        this.adminMode = adminMode;
        setBackground(Theme.LIGHT);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        Object[] cols = adminMode
                ? new Object[]{"Order ID", "Customer", "Date", "Items", "Payment", "Total"}
                : new Object[]{"Order ID", "Date", "Items", "Payment", "Total"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(Theme.title(adminMode ? "All Orders & Sales" : "My Orders"), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        summary.setFont(Theme.HEADING);
        summary.setForeground(Theme.DARK);
        JPanel south = Theme.panel(new BorderLayout());
        south.add(summary, BorderLayout.WEST);
        JPanel buttons = Theme.panel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton view = Theme.button("View receipt");
        JButton reload = Theme.button("Refresh");
        buttons.add(reload);
        buttons.add(view);
        south.add(buttons, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        view.addActionListener(e -> openSelected());
        reload.addActionListener(e -> refresh());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openSelected();
            }
        });
    }

    @Override
    public void refresh() {
        try {
            orders = adminMode ? ctx.orders.allOrders() : ctx.orders.historyFor(ctx.auth.getCurrentUser());
        } catch (Exception ex) {
            Dialogs.error(this, ex);
            return;
        }
        model.setRowCount(0);
        BigDecimal revenue = BigDecimal.ZERO;
        for (Order o : orders) {
            revenue = revenue.add(o.getTotal());
            String id = String.format("#%06d", o.getId());
            if (adminMode) {
                model.addRow(new Object[]{id, o.getCustomerName(), Formats.dateTime(o.getOrderDate()),
                        o.getItemCount(), o.getPaymentMethod(), Formats.money(o.getTotal())});
            } else {
                model.addRow(new Object[]{id, Formats.dateTime(o.getOrderDate()), o.getItemCount(),
                        o.getPaymentMethod(), Formats.money(o.getTotal())});
            }
        }
        if (orders.isEmpty()) {
            summary.setText(adminMode ? "No orders yet." : "You have not placed any orders yet.");
        } else {
            summary.setText(orders.size() + " order(s)  |  " + (adminMode ? "Revenue: " : "Total spent: ")
                    + Formats.money(revenue));
        }
    }

    private void openSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.warn(this, "Please select an order first.");
            return;
        }
        ReceiptDialog.show(this, orders.get(row));
    }
}
