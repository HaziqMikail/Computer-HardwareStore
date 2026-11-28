package computerhardwarestore.view.order;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.Order;
import computerhardwarestore.model.OrderItem;
import computerhardwarestore.util.Formats;
import computerhardwarestore.view.common.Card;
import computerhardwarestore.view.common.Dialogs;
import computerhardwarestore.view.common.PageHeader;
import computerhardwarestore.view.common.Refreshable;
import computerhardwarestore.view.common.Theme;
import computerhardwarestore.view.common.Ui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Order history. Customers see their own orders; in admin mode every customer's orders plus sales figures. */
public class OrdersPanel extends JPanel implements Refreshable {
    private static final String VIEW_RECEIPT = "View receipt";

    private final AppContext ctx;
    private final boolean adminMode;
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel empty = Ui.muted(" ");
    private final JPanel stats = Ui.transparent(new GridLayout(1, 0, 14, 0));
    private final int itemsCol;
    private final int totalCol;
    private final int receiptCol;
    private List<Order> orders = new ArrayList<>();

    public OrdersPanel(AppContext ctx, boolean adminMode) {
        super(new BorderLayout(0, Theme.GAP));
        this.ctx = ctx;
        this.adminMode = adminMode;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

        JButton reload = Ui.secondary("Refresh");
        reload.addActionListener(e -> {
            refresh();
            computerhardwarestore.view.common.Toast.show(this, "Orders refreshed");
        });
        PageHeader header = new PageHeader(adminMode ? "Orders" : "My orders",
                adminMode ? "Every order placed by customers." : "Everything you have bought so far.");
        header.action(reload);

        JPanel top = Ui.transparent(new BorderLayout(0, 12));
        top.add(header, BorderLayout.NORTH);
        top.add(stats, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        Object[] cols = adminMode
                ? new Object[]{"Order", "Customer", "Date", "Items", "Payment", "Total", ""}
                : new Object[]{"Order", "Date", "Items", "Payment", "Total", ""};
        itemsCol = adminMode ? 3 : 2;
        totalCol = cols.length - 2;
        receiptCol = cols.length - 1;
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Ui.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCursor(Cursor.getDefaultCursor());

        DefaultTableCellRenderer total = new DefaultTableCellRenderer();
        total.setHorizontalAlignment(SwingConstants.RIGHT);
        total.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 14));
        table.getColumnModel().getColumn(totalCol).setCellRenderer(total);
        table.getColumnModel().getColumn(receiptCol).setCellRenderer(new LinkRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(itemsCol).setPreferredWidth(280);
        table.getColumnModel().getColumn(totalCol).setPreferredWidth(120);
        table.getColumnModel().getColumn(receiptCol).setPreferredWidth(120);
        table.getColumnModel().getColumn(receiptCol).setMaxWidth(140);

        Card tableCard = new Card(new BorderLayout()).pad(6, 6, 6, 6);
        tableCard.add(Ui.scroll(table), BorderLayout.CENTER);
        empty.setHorizontalAlignment(SwingConstants.CENTER);
        tableCard.add(empty, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == receiptCol) ReceiptDialog.show(OrdersPanel.this, orders.get(row));
            }
        });
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                boolean overLink = table.rowAtPoint(e.getPoint()) >= 0
                        && table.columnAtPoint(e.getPoint()) == receiptCol;
                table.setCursor(Cursor.getPredefinedCursor(overLink ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
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
        BigDecimal revenue = BigDecimal.ZERO;
        int itemCount = 0;
        model.setRowCount(0);
        for (Order o : orders) {
            revenue = revenue.add(o.getTotal());
            itemCount += o.getItemCount();
            String id = String.format("#%06d", o.getId());
            String list = itemList(o);
            if (adminMode) {
                model.addRow(new Object[]{id, o.getCustomerName(), Formats.dateTime(o.getOrderDate()), list,
                        o.getPaymentMethod(), Formats.money(o.getTotal()), VIEW_RECEIPT});
            } else {
                model.addRow(new Object[]{id, Formats.dateTime(o.getOrderDate()), list,
                        o.getPaymentMethod(), Formats.money(o.getTotal()), VIEW_RECEIPT});
            }
        }
        for (int row = 0; row < orders.size(); row++) {
            table.setRowHeight(row, Math.max(46, 18 + 20 * orders.get(row).getItems().size()));
        }

        stats.removeAll();
        stats.add(stat(adminMode ? "Revenue" : "Total spent", Formats.money(revenue)));
        stats.add(stat("Orders", String.valueOf(orders.size())));
        stats.add(stat(adminMode ? "Items sold" : "Items bought", String.valueOf(itemCount)));
        stats.revalidate();
        stats.repaint();
        empty.setText(orders.isEmpty()
                ? (adminMode ? "No orders yet." : "You have not placed any orders yet.") : " ");
    }

    /** "2 × Razer Kraken X" on one line per product, as simple HTML for the table cell. */
    private static String itemList(Order o) {
        StringBuilder sb = new StringBuilder("<html>");
        boolean first = true;
        for (OrderItem i : o.getItems()) {
            if (!first) sb.append("<br>");
            sb.append(i.getQuantity()).append(" × ").append(Formats.html(i.getProductName()));
            first = false;
        }
        return sb.append("</html>").toString();
    }

    private static JPanel stat(String label, String value) {
        Card c = new Card(new BorderLayout(0, 2)).pad(12, 16, 12, 16);
        c.add(Ui.muted(label), BorderLayout.NORTH);
        JLabel v = new JLabel(value);
        v.setFont(Theme.H2);
        c.add(v, BorderLayout.CENTER);
        return c;
    }

    /** Draws the last column as an underlined gold link. */
    private static class LinkRenderer extends DefaultTableCellRenderer {
        LinkRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            super.getTableCellRendererComponent(t, "<html><u>" + v + "</u></html>", sel, focus, r, c);
            setFont(Theme.BOLD);
            setForeground(Theme.GOLD_DARK);
            return this;
        }
    }
}
