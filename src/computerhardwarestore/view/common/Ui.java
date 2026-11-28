package computerhardwarestore.view.common;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;

/** Small factories so every screen builds widgets the same way. */
public final class Ui {
    private Ui() {}

    public static JPanel transparent(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    public static JPanel row(int align, int gap) {
        return transparent(new FlowLayout(align, gap, 0));
    }

    public static JButton primary(String text) {
        return button(text, Theme.ESPRESSO, Color.WHITE, Theme.ESPRESSO_SOFT);
    }

    public static JButton accent(String text) {
        return button(text, Theme.GOLD, Theme.ESPRESSO, Theme.GOLD_DARK);
    }

    public static JButton secondary(String text) {
        JButton b = button(text, Color.WHITE, Theme.TEXT, new Color(0xF3EEE6));
        return b;
    }

    public static JButton danger(String text) {
        return button(text, Theme.DANGER_BG, Theme.DANGER, new Color(0xF6D2CF));
    }

    /** A flat text-style button (for small inline actions). */
    public static JButton link(String text) {
        JButton b = new JButton(text);
        b.putClientProperty("JButton.buttonType", "borderless");
        b.setForeground(Theme.GOLD_DARK);
        b.setFont(Theme.BOLD);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        return b;
    }

    private static JButton button(String text, Color bg, Color fg, Color hover) {
        JButton b = new JButton(text) {
            @Override
            public void updateUI() {
                super.updateUI();
                setContentAreaFilled(true);
            }

            /** A little spare width so text is never replaced by "..." when the display measures it wider. */
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width + 10, d.height);
            }
        };
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(Theme.BOLD);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("FlatLaf.style", "arc: 12; margin: 9,18,9,18; hoverBackground: " + hex(hover)
                + "; pressedBackground: " + hex(hover) + "; disabledBackground: #E9E4DC; disabledText: #A99C92");
        return b;
    }

    private static String hex(Color c) {
        return String.format("#%06X", c.getRGB() & 0xFFFFFF);
    }

    public static JLabel h1(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.H1);
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel h2(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.H2);
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.SMALL);
        l.setForeground(Theme.MUTED);
        return l;
    }

    public static JLabel bold(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BOLD);
        l.setForeground(Theme.TEXT);
        return l;
    }

    /** Small coloured status label such as "In stock". kind: "ok", "warn", "bad", "info". */
    public static JLabel pill(String text, String kind) {
        Color fg;
        Color bg;
        switch (kind) {
            case "ok" -> { fg = Theme.SUCCESS; bg = Theme.SUCCESS_BG; }
            case "warn" -> { fg = Theme.WARNING; bg = Theme.WARNING_BG; }
            case "bad" -> { fg = Theme.DANGER; bg = Theme.DANGER_BG; }
            default -> { fg = Theme.MUTED; bg = new Color(0xEFE9E0); }
        }
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        l.putClientProperty("FlatLaf.style", "arc: 999");
        return l;
    }

    public static void styleTable(JTable t) {
        t.setFont(Theme.BODY);
        t.setRowHeight(40);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);
        JTableHeader h = t.getTableHeader();
        h.setFont(Theme.BOLD);
        h.setReorderingAllowed(false);
        h.setPreferredSize(new java.awt.Dimension(10, 36));
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createEmptyBorder());
        s.getViewport().setOpaque(false);
        s.setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(24);
        return s;
    }
}
