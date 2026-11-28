package computerhardwarestore.view.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** A white rounded panel with a thin border: the basic building block of every page. */
public class Card extends JPanel {
    private Color fill = Theme.CARD;
    private Color outline = Theme.LINE;

    public Card() {
        this(new BorderLayout());
    }

    public Card(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(new EmptyBorder(16, 18, 16, 18));
    }

    public Card fill(Color c) {
        fill = c;
        return this;
    }

    public Card outline(Color c) {
        outline = c;
        return this;
    }

    public Card pad(int top, int left, int bottom, int right) {
        setBorder(new EmptyBorder(top, left, bottom, right));
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.ARC, Theme.ARC);
        if (outline != null) {
            g2.setColor(outline);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.ARC, Theme.ARC);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
