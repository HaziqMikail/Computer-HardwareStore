package computerhardwarestore.view.shell;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.Order;
import computerhardwarestore.model.User;
import computerhardwarestore.view.cart.CartPanel;
import computerhardwarestore.view.cart.CheckoutPanel;
import computerhardwarestore.view.common.NavButton;
import computerhardwarestore.view.common.Navigator;
import computerhardwarestore.view.common.PageHost;
import computerhardwarestore.view.common.Pages;
import computerhardwarestore.view.common.Theme;
import computerhardwarestore.view.common.Ui;
import computerhardwarestore.view.order.OrdersPanel;
import computerhardwarestore.view.order.ReceiptDialog;
import computerhardwarestore.view.shop.ShopPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Customer layout: dark top bar with Shop / Cart / My orders, content underneath. */
public class CustomerShell extends JPanel implements Shell, Navigator {
    private final AppContext ctx;
    private final Runnable onLogout;
    private final PageHost host = new PageHost();
    private final Map<String, NavButton> nav = new LinkedHashMap<>();
    private final NavButton cartButton = new NavButton("Cart");

    public CustomerShell(AppContext ctx, User user, Runnable onLogout) {
        super(new BorderLayout());
        this.ctx = ctx;
        this.onLogout = onLogout;

        host.add(Pages.SHOP, new ShopPanel(ctx, this, true));
        host.add(Pages.CART, new CartPanel(ctx, this));
        host.add(Pages.CHECKOUT, new CheckoutPanel(ctx, this, this::onPaid));
        host.add(Pages.MY_ORDERS, new OrdersPanel(ctx, false));

        add(topBar(user), BorderLayout.NORTH);
        add(host, BorderLayout.CENTER);
    }

    private JPanel topBar(User user) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.ESPRESSO);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

        JPanel left = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        java.net.URL logo = CustomerShell.class.getClassLoader().getResource("images/back5.png");
        if (logo != null) {
            left.add(new JLabel(new ImageIcon(new ImageIcon(logo).getImage()
                    .getScaledInstance(34, 34, java.awt.Image.SCALE_SMOOTH))));
        }
        JLabel brand = new JLabel("Computer & Hardware");
        brand.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        brand.setForeground(java.awt.Color.WHITE);
        left.add(brand);
        left.add(javax.swing.Box.createHorizontalStrut(18));
        left.add(navButton("Shop", Pages.SHOP, null));
        left.add(navButton("Cart", Pages.CART, cartButton));
        left.add(navButton("My orders", Pages.MY_ORDERS, null));
        bar.add(left, BorderLayout.WEST);

        JPanel right = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        JLabel hello = new JLabel("Hi, " + user.getFirstName());
        hello.setFont(Theme.BOLD);
        hello.setForeground(Theme.GOLD);
        JButton logout = Ui.secondary("Log out");
        logout.addActionListener(e -> onLogout.run());
        right.add(hello);
        right.add(logout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private NavButton navButton(String text, String page, NavButton existing) {
        NavButton b = existing != null ? existing : new NavButton(text);
        b.addActionListener(e -> show(page));
        nav.put(page, b);
        return b;
    }

    private void onPaid(Order order) {
        show(Pages.MY_ORDERS);
        ReceiptDialog.show(this, order);
    }

    @Override
    public void show(String page) {
        host.show(page);
        nav.forEach((key, button) -> button.setActive(key.equals(page)
                || (page.equals(Pages.CHECKOUT) && key.equals(Pages.CART))));
    }

    @Override
    public void refreshHeader() {
        int n = ctx.cart.getCart().getTotalQuantity();
        cartButton.setText(n == 0 ? "Cart" : "Cart  (" + n + ")");
    }

    @Override public Component component() { return this; }

    @Override
    public void start() {
        refreshHeader();
        show(Pages.SHOP);
    }
}
