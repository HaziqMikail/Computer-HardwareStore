package computerhardwarestore.view;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.Product;
import computerhardwarestore.util.Formats;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/** Product browsing: search box, category filter, and a grid of product cards with live stock. */
public class ShopPanel extends JPanel implements Refreshable {
    private static final String ALL = "All categories";

    private final AppContext ctx;
    private final Navigator nav;
    private final JTextField search = new JTextField(18);
    private final JComboBox<String> category = new JComboBox<>();
    private final JLabel status = new JLabel(" ");
    private final CardGrid grid = new CardGrid();
    private boolean loadingCategories;

    public ShopPanel(AppContext ctx, Navigator nav) {
        super(new BorderLayout(8, 8));
        this.ctx = ctx;
        this.nav = nav;
        setBackground(Theme.LIGHT);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel bar = Theme.panel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JLabel l = new JLabel("Search:");
        l.setFont(Theme.BODY_BOLD);
        bar.add(l);
        bar.add(search);
        bar.add(category);
        JButton go = Theme.button("Search");
        JButton clear = Theme.button("Clear");
        bar.add(go);
        bar.add(clear);
        add(bar, BorderLayout.NORTH);

        grid.setBackground(Theme.LIGHT);
        JScrollPane scroll = new JScrollPane(grid);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 2));
        add(scroll, BorderLayout.CENTER);

        status.setFont(Theme.BODY);
        add(status, BorderLayout.SOUTH);

        go.addActionListener(e -> load());
        search.addActionListener(e -> load());
        category.addActionListener(e -> {
            if (!loadingCategories) load();
        });
        clear.addActionListener(e -> {
            search.setText("");
            category.setSelectedIndex(0);
            load();
        });
    }

    @Override
    public void refresh() {
        loadCategories();
        load();
    }

    private void loadCategories() {
        Object selected = category.getSelectedItem();
        loadingCategories = true;
        try {
            category.removeAllItems();
            category.addItem(ALL);
            for (String c : ctx.catalog.categories()) category.addItem(c);
            if (selected != null) category.setSelectedItem(selected);
            if (category.getSelectedItem() == null) category.setSelectedIndex(0);
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        } finally {
            loadingCategories = false;
        }
    }

    private void load() {
        String cat = (String) category.getSelectedItem();
        if (ALL.equals(cat)) cat = null;
        try {
            List<Product> products = ctx.catalog.search(search.getText(), cat);
            grid.removeAll();
            for (Product p : products) grid.add(card(p));
            grid.revalidate();
            grid.repaint();
            status.setText(products.isEmpty() ? "No products match your search."
                    : products.size() + " product(s) shown.");
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private JPanel card(Product p) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 3));

        JLabel img = new JLabel(ImageLoader.load(p.getImagePath(), 180, 140), SwingConstants.CENTER);
        img.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));
        card.add(img, BorderLayout.NORTH);

        String stockText;
        String stockColor;
        if (p.getStock() <= 0) {
            stockText = "Out of stock";
            stockColor = "#aa0000";
        } else if (p.getStock() <= 5) {
            stockText = "Only " + p.getStock() + " left";
            stockColor = "#c86400";
        } else {
            stockText = p.getStock() + " in stock";
            stockColor = "#007800";
        }
        StringBuilder html = new StringBuilder("<html><div style='width:190px'>");
        html.append("<b>").append(Formats.html(p.getName())).append("</b><br/>");
        html.append("<font color='#3e1f1b'><b>").append(Formats.money(p.getPrice())).append("</b></font><br/>");
        html.append("<font color='").append(stockColor).append("'><b>").append(stockText).append("</b></font><br/>");
        for (String spec : p.getSpecs().split(", ")) {
            if (!spec.isBlank()) html.append("<small>").append(Formats.html(spec)).append("</small><br/>");
        }
        html.append("</div></html>");
        JLabel info = new JLabel(html.toString());
        info.setVerticalAlignment(SwingConstants.TOP);
        info.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        card.add(info, BorderLayout.CENTER);

        JPanel buy = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        buy.setBackground(Theme.LIGHT);
        int max = Math.max(1, p.getStock());
        JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, max, 1));
        qty.setPreferredSize(new Dimension(60, 28));
        JButton add = Theme.button("Add to Cart");
        add.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        if (p.getStock() <= 0) {
            qty.setEnabled(false);
            add.setEnabled(false);
        }
        add.addActionListener(e -> {
            try {
                qty.commitEdit();
                ctx.cart.add(p, String.valueOf(qty.getValue()));
                nav.refreshHeader();
                Dialogs.info(this, p.getName() + " added to cart.");
            } catch (java.text.ParseException pe) {
                Dialogs.warn(this, "Please enter a whole number for the quantity.");
            } catch (Exception ex) {
                Dialogs.error(this, ex);
                load(); // stock may have changed; show the latest
            }
        });
        buy.add(new JLabel("Qty:"));
        buy.add(qty);
        buy.add(add);
        card.add(buy, BorderLayout.SOUTH);
        return card;
    }

    /** A grid that is always as wide as the viewport (so it never scrolls sideways) with 3 or fewer columns. */
    private static class CardGrid extends JPanel implements Scrollable {
        CardGrid() {
            super(new GridLayout(0, 3, 10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 200; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
