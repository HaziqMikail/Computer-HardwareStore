package computerhardwarestore.view.order;

import computerhardwarestore.model.Order;
import computerhardwarestore.model.OrderItem;
import computerhardwarestore.util.Formats;
import computerhardwarestore.util.ReceiptFormatter;
import computerhardwarestore.view.common.Card;
import computerhardwarestore.view.common.Dialogs;
import computerhardwarestore.view.common.Theme;
import computerhardwarestore.view.common.Ui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Shows a saved order's receipt as a clean summary card, with an option to save it as a text file. */
public final class ReceiptDialog {
    private ReceiptDialog() {}

    public static void show(Component parent, Order order) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Receipt " + orderNumber(order), Dialog.ModalityType.APPLICATION_MODAL);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));

        JPanel top = Ui.transparent(new BorderLayout());
        top.add(view(order), BorderLayout.NORTH);
        root.add(Ui.scroll(top), BorderLayout.CENTER);

        JButton save = Ui.secondary("Save as file");
        JButton close = Ui.primary("Close");
        JPanel bar = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bar.add(save);
        bar.add(close);
        root.add(bar, BorderLayout.SOUTH);
        dialog.setContentPane(root);

        save.addActionListener(e -> save(dialog, order));
        close.addActionListener(e -> dialog.dispose());

        dialog.setSize(500, 640);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /** The receipt content (no buttons). Public so it can be rendered on its own. */
    public static JComponent view(Order o) {
        Card card = new Card(new BorderLayout()).pad(22, 24, 22, 24);
        JPanel c = Ui.transparent(null);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

        JPanel head = Ui.transparent(new BorderLayout());
        JPanel titles = Ui.transparent(null);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(Ui.h2("Receipt"));
        titles.add(Ui.muted("Order " + orderNumber(o)));
        head.add(titles, BorderLayout.WEST);
        JPanel paid = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        paid.add(Ui.pill("Paid", "ok"));
        head.add(paid, BorderLayout.EAST);
        c.add(full(head));
        c.add(Box.createVerticalStrut(16));

        JPanel info = Ui.transparent(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(3, 0, 3, 16);
        addInfo(info, g, 0, "Date", Ui.bold(Formats.dateTime(o.getOrderDate())));
        addInfo(info, g, 1, "Customer", Ui.bold(o.getCustomerName()));
        JPanel pay = Ui.transparent(null);
        pay.setLayout(new BoxLayout(pay, BoxLayout.Y_AXIS));
        pay.add(Ui.bold(o.getPaymentMethod()));
        pay.add(Ui.muted(o.getPaymentDetail()));
        addInfo(info, g, 2, "Payment", pay);
        g.gridx = 2;
        g.gridy = 0;
        g.weightx = 1;
        info.add(Box.createHorizontalGlue(), g);
        c.add(full(info));

        c.add(Box.createVerticalStrut(14));
        c.add(divider());
        c.add(Box.createVerticalStrut(10));
        c.add(full(heading("Items")));
        c.add(Box.createVerticalStrut(6));
        for (OrderItem i : o.getItems()) {
            JPanel row = Ui.transparent(new BorderLayout(10, 0));
            JPanel left = Ui.transparent(null);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.add(Ui.bold(i.getProductName()));
            left.add(Ui.muted(i.getQuantity() + " × " + Formats.money(i.getUnitPrice())));
            row.add(left, BorderLayout.CENTER);
            row.add(Ui.bold(Formats.money(i.getLineTotal())), BorderLayout.EAST);
            row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
            c.add(full(row));
        }

        c.add(Box.createVerticalStrut(6));
        c.add(divider());
        c.add(Box.createVerticalStrut(10));
        c.add(full(amount("Subtotal", Formats.money(o.getSubtotal()), false)));
        c.add(Box.createVerticalStrut(4));
        c.add(full(amount("Delivery", Formats.money(o.getDeliveryCharge()), false)));
        c.add(Box.createVerticalStrut(8));
        c.add(full(amount("Total paid", Formats.money(o.getTotal()), true)));

        c.add(Box.createVerticalStrut(18));
        JLabel thanks = Ui.bold("Thank you for your purchase!");
        thanks.setHorizontalAlignment(SwingConstants.CENTER);
        c.add(full(thanks));
        JLabel eta = Ui.muted("Your items will arrive in 2-3 working days.");
        eta.setHorizontalAlignment(SwingConstants.CENTER);
        c.add(full(eta));

        card.add(c, BorderLayout.CENTER);
        return card;
    }

    private static String orderNumber(Order o) {
        return String.format("#%06d", o.getId());
    }

    private static JLabel heading(String text) {
        return Ui.muted(text.toUpperCase());
    }

    private static void addInfo(JPanel p, GridBagConstraints g, int row, String label, JComponent value) {
        g.gridy = row;
        g.gridx = 0;
        JLabel l = Ui.muted(label);
        l.setPreferredSize(new Dimension(70, l.getPreferredSize().height));
        p.add(l, g);
        g.gridx = 1;
        p.add(value, g);
    }

    private static JPanel amount(String label, String value, boolean big) {
        JPanel r = Ui.transparent(new BorderLayout());
        r.add(big ? Ui.bold(label) : Ui.muted(label), BorderLayout.WEST);
        JLabel v = new JLabel(value);
        v.setFont(big ? Theme.H2 : Theme.BODY);
        r.add(v, BorderLayout.EAST);
        return r;
    }

    private static JPanel divider() {
        JPanel d = new JPanel();
        d.setBackground(Theme.LINE);
        d.setPreferredSize(new Dimension(1, 1));
        return full(d);
    }

    /** Lets a box-layout child stretch to the full width while keeping its own height. */
    private static <T extends JComponent> T full(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(1, c.getPreferredSize().height)));
        return c;
    }

    private static void save(Component parent, Order order) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(String.format("receipt-%06d.txt", order.getId())));
        chooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().contains(".")) file = new File(file.getPath() + ".txt");
        if (file.exists() && !Dialogs.confirm(parent, file.getName() + " already exists. Overwrite it?")) return;
        try {
            Files.writeString(file.toPath(), ReceiptFormatter.format(order), StandardCharsets.UTF_8);
            Dialogs.info(parent, "Receipt saved to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            Dialogs.warn(parent, "Could not save the receipt: " + ex.getMessage());
        }
    }
}
