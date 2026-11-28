package computerhardwarestore.view;

import computerhardwarestore.model.Order;
import computerhardwarestore.util.ReceiptFormatter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.awt.print.PrinterException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Shows a saved order's receipt, with Save and Print. Used after checkout and from order history. */
public final class ReceiptDialog {
    private ReceiptDialog() {}

    public static void show(Component parent, Order order) {
        String text = ReceiptFormatter.format(order);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        area.setCaretPosition(0);

        java.awt.Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Receipt #" + String.format("%06d", order.getId()),
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(area), BorderLayout.CENTER);

        JButton save = Theme.button("Save as file");
        JButton print = Theme.button("Print");
        JButton close = Theme.button("Close");
        JPanel bar = Theme.panel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bar.add(save);
        bar.add(print);
        bar.add(close);
        dialog.add(bar, BorderLayout.SOUTH);

        save.addActionListener(e -> save(dialog, order, text));
        print.addActionListener(e -> {
            try {
                area.print();
            } catch (PrinterException ex) {
                Dialogs.warn(dialog, "Could not print the receipt: " + ex.getMessage());
            }
        });
        close.addActionListener(e -> dialog.dispose());

        dialog.setSize(640, 560);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void save(Component parent, Order order, String text) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(String.format("receipt-%06d.txt", order.getId())));
        chooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().contains(".")) file = new File(file.getPath() + ".txt");
        if (file.exists() && !Dialogs.confirm(parent, file.getName() + " already exists. Overwrite it?")) return;
        try {
            Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
            Dialogs.info(parent, "Receipt saved to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            Dialogs.warn(parent, "Could not save the receipt: " + ex.getMessage());
        }
    }
}
