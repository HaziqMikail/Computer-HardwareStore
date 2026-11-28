package computerhardwarestore.view.admin;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.DashboardStats;
import computerhardwarestore.model.Order;
import computerhardwarestore.model.Product;
import computerhardwarestore.util.Formats;
import computerhardwarestore.view.common.Card;
import computerhardwarestore.view.common.Dialogs;
import computerhardwarestore.view.common.FitLabel;
import computerhardwarestore.view.common.Navigator;
import computerhardwarestore.view.common.PageHeader;
import computerhardwarestore.view.common.Pages;
import computerhardwarestore.view.common.Refreshable;
import computerhardwarestore.view.common.Theme;
import computerhardwarestore.view.common.Ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Admin landing page: key numbers, recent orders and low-stock products, each linking to its page. */
public class DashboardPanel extends JPanel implements Refreshable {
    private final AppContext ctx;
    private final Navigator nav;
    private final JPanel tiles = Ui.transparent(new GridLayout(1, 4, 14, 0));
    private final JPanel lists = Ui.transparent(new GridLayout(1, 2, 14, 0));

    public DashboardPanel(AppContext ctx, Navigator nav) {
        super(new BorderLayout(0, Theme.GAP));
        this.ctx = ctx;
        this.nav = nav;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

        JButton refresh = Ui.secondary("Refresh");
        refresh.addActionListener(e -> {
            refresh();
            computerhardwarestore.view.common.Toast.show(this, "Dashboard refreshed");
        });
        JPanel top = Ui.transparent(new BorderLayout(0, Theme.GAP));
        top.add(new PageHeader("Dashboard", "How the store is doing right now.").action(refresh), BorderLayout.NORTH);
        top.add(tiles, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(lists, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        DashboardStats s;
        try {
            s = ctx.orders.dashboard();
        } catch (Exception ex) {
            Dialogs.error(this, ex);
            return;
        }
        tiles.removeAll();
        tiles.add(tile("Revenue", Formats.money(s.revenue()), "From all orders", Theme.GOLD, Pages.ALL_ORDERS));
        tiles.add(tile("Orders", String.valueOf(s.orderCount()), "Placed so far", Theme.SUCCESS, Pages.ALL_ORDERS));
        tiles.add(tile("Products", String.valueOf(s.productCount()), "In the catalogue", Theme.ESPRESSO_SOFT,
                Pages.INVENTORY));
        tiles.add(tile("Customers", String.valueOf(s.customerCount()), "Registered accounts", Theme.MUTED, null));

        lists.removeAll();
        lists.add(recentOrders(s));
        lists.add(lowStock(s));
        revalidate();
        repaint();
    }

    private JPanel tile(String label, String value, String sub, Color accent, String page) {
        Card c = new Card(new BorderLayout(0, 4)).pad(16, 18, 16, 18);
        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(34, 4));
        JPanel head = Ui.transparent(new BorderLayout(0, 8));
        head.add(bar, BorderLayout.NORTH);
        head.add(Ui.muted(label), BorderLayout.CENTER);
        c.add(head, BorderLayout.NORTH);
        JLabel v = new computerhardwarestore.view.common.FitLabel(value, 28, 16);
        c.add(v, BorderLayout.CENTER);
        c.add(Ui.muted(sub), BorderLayout.SOUTH);
        if (page != null) {
            c.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            c.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { nav.show(page); }
            });
        }
        return c;
    }

    private JPanel recentOrders(DashboardStats s) {
        Card c = new Card(new BorderLayout(0, 10));
        JButton all = Ui.link("View all");
        all.addActionListener(e -> nav.show(Pages.ALL_ORDERS));
        c.add(header("Recent orders", all), BorderLayout.NORTH);
        JPanel list = Ui.transparent(null);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        if (s.recentOrders().isEmpty()) list.add(Ui.muted("No orders yet."));
        for (Order o : s.recentOrders()) {
            JPanel r = Ui.transparent(new BorderLayout(10, 0));
            JPanel left = Ui.transparent(null);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.add(Ui.bold(String.format("#%06d  %s", o.getId(), o.getCustomerName())));
            left.add(FitLabel.muted(Formats.dateTime(o.getOrderDate()) + " • " + o.getPaymentMethod()));
            r.add(left, BorderLayout.CENTER);
            r.add(Ui.bold(Formats.money(o.getTotal())), BorderLayout.EAST);
            r.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            r.setAlignmentX(Component.LEFT_ALIGNMENT);
            r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            list.add(r);
        }
        c.add(list, BorderLayout.CENTER);
        return c;
    }

    private JPanel lowStock(DashboardStats s) {
        Card c = new Card(new BorderLayout(0, 10));
        JButton manage = Ui.link("Manage");
        manage.addActionListener(e -> nav.show(Pages.INVENTORY));
        c.add(header("Low stock", manage), BorderLayout.NORTH);
        JPanel list = Ui.transparent(null);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        if (s.lowStock().isEmpty()) list.add(Ui.muted("Everything is well stocked."));
        for (Product p : s.lowStock()) {
            JPanel r = Ui.transparent(new BorderLayout(10, 0));
            r.add(Ui.bold(p.getName()), BorderLayout.CENTER);
            r.add(p.isOutOfStock() ? Ui.pill("Out of stock", "bad") : Ui.pill(p.getStock() + " left", "warn"),
                    BorderLayout.EAST);
            r.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            r.setAlignmentX(Component.LEFT_ALIGNMENT);
            r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            list.add(r);
        }
        list.add(Box.createVerticalGlue());
        c.add(list, BorderLayout.CENTER);
        return c;
    }

    private static JPanel header(String title, JButton action) {
        JPanel h = Ui.transparent(new BorderLayout());
        h.add(Ui.h2(title), BorderLayout.WEST);
        h.add(action, BorderLayout.EAST);
        return h;
    }
}
