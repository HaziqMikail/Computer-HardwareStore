package computerhardwarestore.view.shop;

import computerhardwarestore.model.Product;
import computerhardwarestore.util.Formats;
import computerhardwarestore.view.common.ImageLoader;
import computerhardwarestore.view.common.Theme;
import computerhardwarestore.view.common.Ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Product details pop-up: large image, full specifications, stock, and (for customers) add to cart. */
public final class ProductDialog {
    private ProductDialog() {}

    public static void show(Component parent, Product p, boolean canBuy, IntConsumer onAdd) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, p.getName(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(view(p, canBuy, qty -> {
            dialog.dispose();
            onAdd.accept(qty);
        }, dialog::dispose));
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /** The dialog content. Public so it can be rendered on its own. */
    public static JComponent view(Product p, boolean canBuy, IntConsumer onAdd, Runnable onClose) {
        JPanel root = new JPanel(new BorderLayout(22, 0));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JLabel img = new JLabel(ImageLoader.load(p.getImagePath(), 280, 240), SwingConstants.CENTER);
        img.setOpaque(true);
        img.setBackground(new Color(0xFAF7F2));
        img.setPreferredSize(new Dimension(280, 240));
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(img, BorderLayout.NORTH);
        root.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout(0, 14)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(340, super.getPreferredSize().height);
            }
        };
        right.setOpaque(false);

        JPanel info = Ui.transparent(null);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(left(Ui.muted(p.getCategory().toUpperCase())));
        info.add(Box.createVerticalStrut(2));
        JLabel name = new JLabel("<html><div style='width:320px'>" + Formats.html(p.getName()) + "</div></html>");
        name.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        info.add(left(name));
        info.add(Box.createVerticalStrut(8));
        JLabel price = new JLabel(Formats.money(p.getPrice()));
        price.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        price.setForeground(Theme.GOLD_DARK);
        info.add(left(price));
        info.add(Box.createVerticalStrut(8));
        JPanel stock = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 0, 0));
        stock.add(p.isOutOfStock() ? Ui.pill("Out of stock", "bad")
                : p.isLowStock() ? Ui.pill("Only " + p.getStock() + " left", "warn")
                : Ui.pill(p.getStock() + " in stock", "ok"));
        info.add(left(stock));
        info.add(Box.createVerticalStrut(16));
        info.add(left(Ui.muted("SPECIFICATIONS")));
        info.add(Box.createVerticalStrut(6));
        info.add(left(specs(p.getSpecs())));
        info.add(Box.createVerticalGlue());
        right.add(info, BorderLayout.CENTER);

        JPanel actions = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        if (canBuy) {
            JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, Math.max(1, p.getStock()), 1));
            qty.setPreferredSize(new Dimension(72, 40));
            JButton add = Ui.accent("Add to cart");
            if (p.isOutOfStock()) {
                qty.setEnabled(false);
                add.setEnabled(false);
            }
            add.addActionListener(e -> {
                try {
                    qty.commitEdit();
                    onAdd.accept((Integer) qty.getValue());
                } catch (java.text.ParseException ex) {
                    qty.setValue(1);
                }
            });
            actions.add(qty);
            actions.add(add);
        }
        JButton close = Ui.secondary("Close");
        close.addActionListener(e -> onClose.run());
        actions.add(close);
        right.add(actions, BorderLayout.SOUTH);

        root.add(right, BorderLayout.CENTER);
        return root;
    }

    private static JPanel specs(String specs) {
        JPanel grid = Ui.transparent(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(3, 0, 3, 14);
        int row = 0;
        for (String spec : specs.split(", ")) {
            if (spec.isBlank()) continue;
            int cut = spec.indexOf(": ");
            g.gridy = row++;
            if (cut < 0) {
                g.gridx = 0;
                g.gridwidth = 2;
                grid.add(Ui.bold(spec), g);
                g.gridwidth = 1;
            } else {
                g.gridx = 0;
                grid.add(Ui.muted(spec.substring(0, cut)), g);
                g.gridx = 1;
                grid.add(Ui.bold("<html><div style='width:190px'>" + Formats.html(spec.substring(cut + 2))
                        + "</div></html>"), g);
            }
        }
        if (row == 0) {
            g.gridx = 0;
            grid.add(Ui.muted("No specifications listed."), g);
        }
        g.gridx = 2;
        g.gridy = 0;
        g.weightx = 1;
        grid.add(Box.createHorizontalGlue(), g);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, grid.getPreferredSize().height));
        return grid;
    }

    private static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }
}
