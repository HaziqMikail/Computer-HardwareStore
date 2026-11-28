package computerhardwarestore.view.shell;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.User;
import computerhardwarestore.view.admin.DashboardPanel;
import computerhardwarestore.view.admin.InventoryPanel;
import computerhardwarestore.view.common.NavButton;
import computerhardwarestore.view.common.Navigator;
import computerhardwarestore.view.common.PageHost;
import computerhardwarestore.view.common.Pages;
import computerhardwarestore.view.common.Theme;
import computerhardwarestore.view.common.Ui;
import computerhardwarestore.view.order.OrdersPanel;
import computerhardwarestore.view.shop.ShopPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Admin layout: dark sidebar with Dashboard / Inventory / Orders / Shop preview, content on the right. */
public class AdminShell extends JPanel implements Shell, Navigator {
    private final PageHost host = new PageHost();
    private final Map<String, NavButton> nav = new LinkedHashMap<>();

    public AdminShell(AppContext ctx, User user, Runnable onLogout) {
        super(new BorderLayout());
        host.add(Pages.DASHBOARD, new DashboardPanel(ctx, this));
        host.add(Pages.INVENTORY, new InventoryPanel(ctx));
        host.add(Pages.ALL_ORDERS, new OrdersPanel(ctx, true));
        host.add(Pages.SHOP, new ShopPanel(ctx, this, false));

        add(sidebar(user, onLogout), BorderLayout.WEST);
        add(host, BorderLayout.CENTER);
    }

    private JPanel sidebar(User user, Runnable onLogout) {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.ESPRESSO);
        side.setBorder(BorderFactory.createEmptyBorder(20, 14, 16, 14));
        side.setPreferredSize(new Dimension(260, 10));

        JPanel brand = Ui.transparent(new BorderLayout(10, 0));
        java.net.URL logo = AdminShell.class.getClassLoader().getResource("images/back5.png");
        if (logo != null) {
            brand.add(new JLabel(new ImageIcon(new ImageIcon(logo).getImage()
                    .getScaledInstance(36, 36, java.awt.Image.SCALE_SMOOTH))), BorderLayout.WEST);
        }
        JPanel names = Ui.transparent(null);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel b = new JLabel("Computer & Hardware");
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        b.setForeground(java.awt.Color.WHITE);
        JLabel role = new JLabel("ADMIN");
        role.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        role.setForeground(Theme.GOLD);
        names.add(b);
        names.add(role);
        brand.add(names, BorderLayout.CENTER);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        side.add(brand);
        side.add(Box.createVerticalStrut(26));

        side.add(item("Dashboard", Pages.DASHBOARD));
        side.add(Box.createVerticalStrut(4));
        side.add(item("Inventory", Pages.INVENTORY));
        side.add(Box.createVerticalStrut(4));
        side.add(item("Orders & sales", Pages.ALL_ORDERS));
        side.add(Box.createVerticalStrut(4));
        side.add(item("Shop preview", Pages.SHOP));
        side.add(Box.createVerticalGlue());

        JLabel who = new JLabel(user.getFullName());
        who.setFont(Theme.BOLD);
        who.setForeground(java.awt.Color.WHITE);
        who.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(who);
        JLabel acct = new JLabel("Signed in as " + user.getUsername());
        acct.setFont(Theme.SMALL);
        acct.setForeground(new java.awt.Color(0xBFB1A5));
        acct.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(acct);
        side.add(Box.createVerticalStrut(12));
        JButton logout = Ui.secondary("Log out");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logout.addActionListener(e -> onLogout.run());
        side.add(logout);
        return side;
    }

    private NavButton item(String text, String page) {
        NavButton b = new NavButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.addActionListener(e -> show(page));
        nav.put(page, b);
        return b;
    }

    @Override
    public void show(String page) {
        host.show(page);
        nav.forEach((key, button) -> button.setActive(key.equals(page)));
    }

    @Override public void refreshHeader() { /* admins have no cart */ }

    @Override public Component component() { return this; }

    @Override public void start() { show(Pages.DASHBOARD); }
}
