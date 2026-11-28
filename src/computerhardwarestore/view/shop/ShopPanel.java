package computerhardwarestore.view.shop;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.Product;
import computerhardwarestore.util.Formats;
import computerhardwarestore.view.common.Card;
import computerhardwarestore.view.common.Dialogs;
import computerhardwarestore.view.common.ImageLoader;
import computerhardwarestore.view.common.Navigator;
import computerhardwarestore.view.common.Pages;
import computerhardwarestore.view.common.Refreshable;
import computerhardwarestore.view.common.Theme;
import computerhardwarestore.view.common.Toast;
import computerhardwarestore.view.common.Ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Product browsing: welcome banner, search, category chips, sort, and a grid of clickable product cards. */
public class ShopPanel extends JPanel implements Refreshable {
    private static final String[] SORTS = {"Featured", "Price: low to high", "Price: high to low", "Name A-Z"};

    private final AppContext ctx;
    private final Navigator nav;
    private final boolean canBuy;
    private final JTextField search = new JTextField();
    private final JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JComboBox<String> sort = new JComboBox<>(SORTS);
    private final JCheckBox inStockOnly = new JCheckBox("In stock only");
    private final JLabel count = Ui.muted(" ");
    private final CardGrid grid = new CardGrid();
    private String category; // null = all

    public ShopPanel(AppContext ctx, Navigator nav, boolean canBuy) {
        super(new BorderLayout(0, 12));
        this.ctx = ctx;
        this.nav = nav;
        this.canBuy = canBuy;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));

        JPanel top = Ui.transparent(null);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JPanel banner = banner();
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(banner);
        top.add(Box.createVerticalStrut(14));

        JPanel bar = Ui.transparent(new BorderLayout(10, 0));
        search.putClientProperty("JTextField.placeholderText", "Search laptops, keyboards, RAM, SSD...");
        search.putClientProperty("JTextField.showClearButton", true);
        search.setPreferredSize(new Dimension(300, 38));
        JButton go = Ui.primary("Search");
        bar.add(search, BorderLayout.CENTER);
        JPanel barRight = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        inStockOnly.setOpaque(false);
        inStockOnly.setFont(Theme.BODY);
        barRight.add(inStockOnly);
        barRight.add(sort);
        barRight.add(go);
        bar.add(barRight, BorderLayout.EAST);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        top.add(bar);
        top.add(Box.createVerticalStrut(12));

        // The chips get the full width; if an admin adds many categories the row scrolls instead of hiding any.
        chips.setOpaque(false);
        javax.swing.JScrollPane chipScroll = new javax.swing.JScrollPane(chips,
                javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chipScroll.setBorder(BorderFactory.createEmptyBorder());
        chipScroll.setOpaque(false);
        chipScroll.getViewport().setOpaque(false);
        chipScroll.getHorizontalScrollBar().setUnitIncrement(30);
        chipScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        chipScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        chipScroll.setPreferredSize(new Dimension(10, 52));
        top.add(chipScroll);
        add(top, BorderLayout.NORTH);

        add(Ui.scroll(grid), BorderLayout.CENTER);
        add(count, BorderLayout.SOUTH);

        go.addActionListener(e -> load());
        search.addActionListener(e -> load());
        sort.addActionListener(e -> load());
        inStockOnly.addActionListener(e -> load());
    }

    private JPanel banner() {
        JPanel b = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.ESPRESSO, getWidth(), 0, new Color(0x5A3A2E)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.ARC + 6, Theme.ARC + 6);
                g2.setColor(new Color(217, 164, 65, 40));
                g2.fillOval(getWidth() - 150, -70, 240, 240);
                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        JPanel text = Ui.transparent(null);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        String who = ctx.auth.getCurrentUser() == null ? "" : ctx.auth.getCurrentUser().getFirstName();
        JLabel hello = new JLabel(canBuy ? (who.isEmpty() ? "Welcome" : "Welcome back, " + who)
                : "Customer view (preview)");
        hello.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        hello.setForeground(Color.WHITE);
        JLabel sub = new JLabel(canBuy ? "Find the right part for your build. Everything ships in 2-3 days."
                : "This is what customers see. Adding to the cart is disabled here.");
        sub.setFont(Theme.BODY);
        sub.setForeground(new Color(0xEADFD2));
        text.add(hello);
        text.add(Box.createVerticalStrut(3));
        text.add(sub);
        b.add(text, BorderLayout.WEST);

        if (canBuy) {
            JButton cart = Ui.accent("View cart");
            cart.addActionListener(e -> nav.show(Pages.CART));
            JPanel wrap = Ui.transparent(new java.awt.GridBagLayout());
            wrap.add(cart);
            b.add(wrap, BorderLayout.EAST);
        }
        return b;
    }

    @Override
    public void refresh() {
        loadChips();
        load();
    }

    private void loadChips() {
        chips.removeAll();
        try {
            chips.add(chip("All", null));
            for (String c : ctx.catalog.categories()) chips.add(chip(c, c));
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
        chips.revalidate();
        chips.repaint();
    }

    private JButton chip(String text, String value) {
        boolean selected = value == null ? category == null : value.equals(category);
        JButton b = selected ? Ui.primary(text) : Ui.secondary(text);
        b.putClientProperty("FlatLaf.style", "arc: 999; margin: 5,16,5,16");
        b.addActionListener(e -> {
            category = value;
            loadChips();
            load();
        });
        return b;
    }

    private void load() {
        try {
            List<Product> products = new java.util.ArrayList<>(ctx.catalog.search(search.getText(), category));
            if (inStockOnly.isSelected()) products.removeIf(Product::isOutOfStock);
            switch (sort.getSelectedIndex()) {
                case 1 -> products.sort(Comparator.comparing(Product::getPrice));
                case 2 -> products.sort(Comparator.comparing(Product::getPrice).reversed());
                case 3 -> products.sort(Comparator.comparing(p -> p.getName().toLowerCase()));
                default -> { /* DAO order: category, then name */ }
            }
            grid.removeAll();
            for (Product p : products) grid.add(card(p));
            if (products.isEmpty()) {
                JLabel none = Ui.muted("No products match your search.");
                none.setHorizontalAlignment(SwingConstants.CENTER);
                grid.add(none);
            }
            grid.revalidate();
            grid.repaint();
            count.setText(products.size() + " product" + (products.size() == 1 ? "" : "s"));
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private void addToCart(Product p, int quantity) {
        try {
            ctx.cart.add(p, String.valueOf(quantity));
            nav.refreshHeader();
            Toast.show(this, p.getName() + " added to cart");
        } catch (Exception ex) {
            Dialogs.error(this, ex);
            load(); // stock may have changed; show the latest
        }
    }

    private JPanel card(Product p) {
        Card card = new Card(new BorderLayout(0, 8)).pad(12, 12, 12, 12);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel img = new JLabel(ImageLoader.load(p.getImagePath(), 220, 150), SwingConstants.CENTER);
        img.setOpaque(true);
        img.setBackground(new Color(0xFAF7F2));
        card.add(img, BorderLayout.NORTH);

        JPanel info = Ui.transparent(null);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel cat = Ui.muted(p.getCategory().toUpperCase());
        cat.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(cat);
        JLabel name = Ui.bold("<html>" + Formats.html(p.getName()) + "</html>");
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(Box.createVerticalStrut(2));
        info.add(name);
        info.add(Box.createVerticalStrut(4));
        JLabel specs = Ui.muted(specLines(p.getSpecs()));
        specs.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(specs);
        card.add(info, BorderLayout.CENTER);

        JPanel bottom = Ui.transparent(new BorderLayout(0, 8));
        JPanel priceRow = Ui.transparent(new BorderLayout());
        JLabel price = new JLabel(Formats.money(p.getPrice()));
        price.setFont(Theme.H2);
        price.setForeground(Theme.TEXT);
        priceRow.add(price, BorderLayout.WEST);
        priceRow.add(stockPill(p), BorderLayout.EAST);
        bottom.add(priceRow, BorderLayout.NORTH);

        JPanel buy = Ui.transparent(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.fill = java.awt.GridBagConstraints.HORIZONTAL;
        g.gridy = 0;
        g.insets = new java.awt.Insets(0, 0, 0, 8);
        JButton details = Ui.secondary("View details");
        details.putClientProperty("FlatLaf.style", "arc: 12; margin: 8,10,8,10; hoverBackground: #F3EEE6");
        details.addActionListener(e -> openDetails(p));
        g.gridx = 0;
        g.weightx = 0;
        buy.add(details, g);
        if (canBuy) {
            JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, Math.max(1, p.getStock()), 1));
            qty.setPreferredSize(new Dimension(64, 38));
            JButton add = Ui.accent("Add to cart");
            add.putClientProperty("FlatLaf.style", "arc: 12; margin: 8,10,8,10; hoverBackground: #B9852A");
            if (p.isOutOfStock()) {
                qty.setEnabled(false);
                add.setEnabled(false);
            }
            add.addActionListener(e -> {
                try {
                    qty.commitEdit();
                    addToCart(p, (Integer) qty.getValue());
                } catch (java.text.ParseException pe) {
                    Dialogs.warn(this, "Please enter a whole number for the quantity.");
                }
            });
            g.gridx = 1;
            buy.add(qty, g);
            g.gridx = 2;
            g.weightx = 1;
            g.insets = new java.awt.Insets(0, 0, 0, 0);
            buy.add(add, g);
        }
        bottom.add(buy, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);

        // Clicking the picture or the text opens the details; the buttons keep their own behaviour.
        MouseAdapter open = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openDetails(p); }
        };
        img.addMouseListener(open);
        info.addMouseListener(open);
        name.addMouseListener(open);
        specs.addMouseListener(open);
        return card;
    }

    private void openDetails(Product p) {
        ProductDialog.show(this, p, canBuy, qty -> addToCart(p, qty));
    }

    private static JLabel stockPill(Product p) {
        if (p.isOutOfStock()) return Ui.pill("Out of stock", "bad");
        if (p.isLowStock()) return Ui.pill("Only " + p.getStock() + " left", "warn");
        return Ui.pill(p.getStock() + " in stock", "ok");
    }

    /** Two short spec lines as a fixed-height HTML label so every card has the same height. */
    private static String specLines(String specs) {
        String[] parts = specs.split(", ");
        StringBuilder sb = new StringBuilder("<html>");
        for (int i = 0; i < 2; i++) {
            if (i > 0) sb.append("<br>");
            String s = i < parts.length ? parts[i] : " ";
            sb.append(Formats.html(s.length() > 34 ? s.substring(0, 31) + "..." : s));
        }
        return sb.append("</html>").toString();
    }


    /** Card grid that follows the window width: 2 to 4 columns, never scrolls sideways. */
    private static class CardGrid extends JPanel implements Scrollable {
        CardGrid() {
            super(new GridLayout(0, 3, 16, 16));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 4));
        }

        /** Picks the column count from the current width. Returns true if it changed. */
        private boolean updateColumns() {
            int cols = Math.max(2, Math.min(4, getWidth() / 330));
            GridLayout layout = (GridLayout) getLayout();
            if (layout.getColumns() == cols) return false;
            layout.setColumns(cols);
            return true;
        }

        @Override
        public Dimension getPreferredSize() {
            updateColumns(); // measure with the column count that will actually be used
            return super.getPreferredSize();
        }

        @Override
        public void doLayout() {
            if (updateColumns()) {
                invalidate();
                SwingUtilities.invokeLater(this::revalidate);
            }
            super.doLayout();
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 240; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
