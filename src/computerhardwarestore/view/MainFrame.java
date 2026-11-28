package computerhardwarestore.view;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.Order;
import computerhardwarestore.model.User;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/** The single application window: header with navigation, menu bar, and a card area holding every screen. */
public class MainFrame extends JFrame implements Navigator {
    public static final String AUTH = "auth";
    public static final String SHOP = "shop";
    public static final String CART = "cart";
    public static final String CHECKOUT = "checkout";
    public static final String MY_ORDERS = "myorders";
    public static final String INVENTORY = "inventory";
    public static final String ALL_ORDERS = "allorders";

    private final AppContext ctx;
    private final CardLayout cards = new CardLayout();
    private final JPanel body = new JPanel(cards);
    private final Map<String, Refreshable> refreshables = new HashMap<>();
    private final JPanel header = new JPanel(new BorderLayout());
    private final JLabel welcome = new JLabel(" ");
    private final JMenuBar menuBar = new JMenuBar();
    private final AuthPanel authPanel;
    private final JLabel cartLabel = new JLabel(" ");
    private final JMenuItem cartItem = new JMenuItem("Cart");

    public MainFrame(AppContext ctx) {
        super("CodeFusion - Computer & Hardware Store");
        this.ctx = ctx;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setSize(1100, 760);
        java.net.URL logo = MainFrame.class.getClassLoader().getResource("images/back5.png");
        if (logo != null) setIconImage(new ImageIcon(logo).getImage());

        authPanel = new AuthPanel(ctx, this::onLoggedIn);
        body.add(authPanel, AUTH);
        add(buildHeader(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        header.setVisible(false);
        setJMenuBar(menuBar);
        menuBar.setVisible(false);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, Math.max(0, (screen.height - getHeight()) / 2));
        cards.show(body, AUTH);
    }

    private JPanel buildHeader() {
        header.setBackground(Theme.DARK);
        header.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        java.net.URL logo = MainFrame.class.getClassLoader().getResource("images/back5.png");
        if (logo != null) {
            left.add(new JLabel(new ImageIcon(new ImageIcon(logo).getImage()
                    .getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH))));
        }
        JLabel brand = new JLabel("CodeFusion");
        brand.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 26));
        brand.setForeground(java.awt.Color.WHITE);
        left.add(brand);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        right.setOpaque(false);
        welcome.setForeground(Theme.LIGHT);
        welcome.setFont(Theme.BODY_BOLD);
        cartLabel.setForeground(java.awt.Color.WHITE);
        cartLabel.setFont(Theme.BODY_BOLD);
        right.add(welcome);
        right.add(cartLabel);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void onLoggedIn(User user) {
        // Build role-specific screens fresh for each login.
        for (String key : new String[]{SHOP, CART, CHECKOUT, MY_ORDERS, INVENTORY, ALL_ORDERS}) {
            refreshables.remove(key);
        }
        for (java.awt.Component c : body.getComponents()) {
            if (c != authPanel) body.remove(c);
        }
        menuBar.removeAll();

        JMenu file = new JMenu("File");
        JMenuItem logout = new JMenuItem("Log out");
        JMenuItem exit = new JMenuItem("Exit");
        logout.addActionListener(e -> logout());
        exit.addActionListener(e -> System.exit(0));
        file.add(logout);
        file.add(exit);
        menuBar.add(file);

        if (user.isAdmin()) {
            register(INVENTORY, new InventoryPanel(ctx));
            register(ALL_ORDERS, new OrdersPanel(ctx, true));
            register(SHOP, new ShopPanel(ctx, this)); // admins may also look at the shop front
            JMenu admin = new JMenu("Admin");
            admin.add(menuItem("Inventory", INVENTORY));
            admin.add(menuItem("All orders & sales", ALL_ORDERS));
            menuBar.add(admin);
            JMenu view = new JMenu("View");
            view.add(menuItem("Shop preview", SHOP));
            menuBar.add(view);
            cartLabel.setText(" ");
            welcome.setText("Administrator: " + user.getFullName());
        } else {
            register(SHOP, new ShopPanel(ctx, this));
            register(CART, new CartPanel(ctx, this));
            register(CHECKOUT, new CheckoutPanel(ctx, this, this::onPaid));
            register(MY_ORDERS, new OrdersPanel(ctx, false));
            JMenu shop = new JMenu("Shop");
            shop.add(menuItem("Browse products", SHOP));
            cartItem.addActionListener(e -> show(CART));
            shop.add(cartItem);
            shop.add(menuItem("My orders", MY_ORDERS));
            menuBar.add(shop);
            welcome.setText("Welcome, " + user.getFirstName());
        }
        header.setVisible(true);
        menuBar.setVisible(true);
        refreshHeader();
        show(user.isAdmin() ? INVENTORY : SHOP);
    }

    private JMenuItem menuItem(String text, String screen) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> show(screen));
        return item;
    }

    private void register(String key, javax.swing.JComponent panel) {
        body.add(panel, key);
        if (panel instanceof Refreshable r) refreshables.put(key, r);
    }

    private void onPaid(Order order) {
        show(MY_ORDERS);
        ReceiptDialog.show(this, order);
    }

    private void logout() {
        ctx.auth.logout();
        ctx.cart.clear();
        header.setVisible(false);
        menuBar.setVisible(false);
        authPanel.reset();
        cards.show(body, AUTH);
    }

    @Override
    public void show(String screen) {
        Refreshable r = refreshables.get(screen);
        if (r != null) r.refresh();
        cards.show(body, screen);
    }

    @Override
    public void refreshHeader() {
        User u = ctx.auth.getCurrentUser();
        if (u == null || u.isAdmin()) return;
        int n = ctx.cart.getCart().getTotalQuantity();
        cartLabel.setText("Cart: " + n + " item" + (n == 1 ? "" : "s"));
        cartItem.setText("Cart (" + n + ")");
    }
}
