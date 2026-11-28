package computerhardwarestore.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

/** The shared brown/gold look: colours, fonts and small factory methods for consistently styled widgets. */
public final class Theme {
    public static final Color DARK = new Color(62, 31, 27);
    public static final Color LIGHT = new Color(225, 193, 110);
    public static final Color BORDER = new Color(139, 69, 19);
    public static final Color CREAM = new Color(250, 243, 224);
    public static final Color GOOD = new Color(0, 120, 0);
    public static final Color WARN = new Color(200, 100, 0);
    public static final Color BAD = new Color(170, 0, 0);

    public static final Font TITLE = new Font("Arial", Font.BOLD, 28);
    public static final Font HEADING = new Font("Arial", Font.BOLD, 18);
    public static final Font BODY = new Font("Arial", Font.PLAIN, 14);
    public static final Font BODY_BOLD = new Font("Arial", Font.BOLD, 14);

    private Theme() {}

    /** Call once before creating any window. */
    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // keep the default look and feel
        }
        UIManager.put("Panel.background", LIGHT);
        UIManager.put("OptionPane.background", LIGHT);
        UIManager.put("OptionPane.messageForeground", Color.BLACK);
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BODY_BOLD);
        UIManager.put("Button.background", DARK);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Label.font", BODY);
        UIManager.put("TextField.font", BODY);
        UIManager.put("PasswordField.font", BODY);
        UIManager.put("TextArea.font", BODY);
        UIManager.put("ComboBox.font", BODY);
        UIManager.put("CheckBox.font", BODY);
        UIManager.put("CheckBox.background", LIGHT);
        UIManager.put("TabbedPane.font", BODY_BOLD);
        UIManager.put("TabbedPane.selected", CREAM);
        UIManager.put("TabbedPane.contentAreaColor", LIGHT);
        UIManager.put("Menu.font", BODY_BOLD);
        UIManager.put("MenuItem.font", BODY);
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setBackground(DARK);
        b.setForeground(Color.WHITE);
        b.setFont(BODY_BOLD);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(7, 16, 7, 16)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(TITLE);
        l.setForeground(DARK);
        return l;
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(HEADING);
        l.setForeground(DARK);
        return l;
    }

    public static JPanel panel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(LIGHT);
        return p;
    }

    public static void styleTable(JTable t) {
        t.setFont(BODY);
        t.setRowHeight(26);
        t.setSelectionBackground(DARK);
        t.setSelectionForeground(Color.WHITE);
        t.setGridColor(new Color(210, 190, 150));
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(DARK);
        h.setForeground(Color.WHITE);
        h.setFont(BODY_BOLD);
        h.setReorderingAllowed(false);
    }
}
