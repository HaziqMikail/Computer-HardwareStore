package computerhardwarestore.view.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/** Page title + subtitle on the left, optional action buttons on the right. */
public class PageHeader extends JPanel {
    private final JPanel actions = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));

    public PageHeader(String title, String subtitle) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.GAP, 0));
        JPanel text = Ui.transparent(null);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Ui.h1(title));
        if (subtitle != null && !subtitle.isEmpty()) {
            text.add(javax.swing.Box.createVerticalStrut(2));
            text.add(Ui.muted(subtitle));
        }
        add(text, BorderLayout.WEST);
        add(actions, BorderLayout.EAST);
    }

    public PageHeader action(Component c) {
        actions.add(c);
        return this;
    }
}
