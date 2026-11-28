package computerhardwarestore.view.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.JLabel;

/** A label that shrinks its font so the whole text always fits the width it is given (never "..."). */
public class FitLabel extends JLabel {
    private final int maxSize;
    private final int minSize;
    private final int style;

    public FitLabel(String text, int maxSize, int minSize) {
        this(text, maxSize, minSize, Font.BOLD);
    }

    public FitLabel(String text, int maxSize, int minSize, int style) {
        super(text);
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.style = style;
        setFont(new Font("Segoe UI", style, maxSize));
    }

    /** A small grey "muted" line that shrinks instead of being cut off. */
    public static FitLabel muted(String text) {
        FitLabel l = new FitLabel(text, 12, 9, Font.PLAIN);
        l.setForeground(Theme.MUTED);
        return l;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(40, d.height); // width is decided by the layout; the text adapts to it
    }

    @Override
    protected void paintComponent(Graphics g) {
        int available = Math.max(1, getWidth() - getInsets().left - getInsets().right);
        Font f = new Font("Segoe UI", style, maxSize);
        for (int size = maxSize; size >= minSize; size--) {
            f = f.deriveFont((float) size);
            FontMetrics fm = getFontMetrics(f);
            if (fm.stringWidth(getText()) <= available) break;
        }
        super.setFont(f);
        super.paintComponent(g);
    }
}
