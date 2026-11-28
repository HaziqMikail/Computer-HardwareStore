package computerhardwarestore.view.common;

import com.formdev.flatlaf.ui.FlatLabelUI;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.ComponentUI;

/**
 * Label UI that reports a few pixels more width than the text needs. Windows display scaling (125%, 150%, ...) can
 * make Swing measure text a little narrower than it is painted, which shows up as "My ord..." instead of
 * "My orders". The spare width removes that for every label in the app.
 */
public class PaddedLabelUI extends FlatLabelUI {
    private static final int EXTRA = 8;

    protected PaddedLabelUI() {
        super(false);
    }

    public static ComponentUI createUI(JComponent c) {
        return new PaddedLabelUI();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        if (c instanceof JLabel l && l.getText() != null && !l.getText().isEmpty()) {
            return new Dimension(d.width + EXTRA, d.height);
        }
        return d;
    }
}
