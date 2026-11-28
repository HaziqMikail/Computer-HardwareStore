package computerhardwarestore.view.common;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/** A navigation entry for the top bar / sidebar that highlights when its page is active. */
public class NavButton extends JButton {
    private boolean active;

    public NavButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setFont(Theme.BOLD);
        setForeground(new Color(0xEADFD2));
        setHorizontalAlignment(LEFT);
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** A little spare width so text is never replaced by "..." when the display measures it wider. */
    @Override
    public java.awt.Dimension getPreferredSize() {
        java.awt.Dimension d = super.getPreferredSize();
        return new java.awt.Dimension(d.width + 12, d.height);
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? Theme.GOLD : new Color(0xEADFD2));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (active) {
            g2.setColor(new Color(217, 164, 65, 46));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(255, 255, 255, 24));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
