package computerhardwarestore.view.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** A short message that fades in at the bottom of the window and disappears by itself. */
public final class Toast {
    private static JWindow current;

    private Toast() {}

    public static void show(Component anchor, String message) {
        show(anchor, message, Theme.ESPRESSO);
    }

    public static void error(Component anchor, String message) {
        show(anchor, message, Theme.DANGER);
    }

    private static void show(Component anchor, String message, Color color) {
        Window owner = anchor == null ? null : SwingUtilities.getWindowAncestor(anchor);
        if (owner == null || !owner.isShowing()) return;
        if (current != null) current.dispose();

        JWindow w = new JWindow(owner);
        w.setFocusableWindowState(false);
        w.setBackground(new Color(0, 0, 0, 0));
        Card card = new Card().fill(color).outline(null).pad(12, 22, 12, 22);
        JLabel label = new JLabel(message);
        label.setFont(Theme.BOLD);
        label.setForeground(Color.WHITE);
        card.add(label);
        w.setContentPane(card);
        w.pack();
        w.setLocation(owner.getX() + (owner.getWidth() - w.getWidth()) / 2,
                owner.getY() + owner.getHeight() - w.getHeight() - 60);
        w.setVisible(true);
        current = w;

        Timer t = new Timer(2200, e -> {
            w.dispose();
            if (current == w) current = null;
        });
        t.setRepeats(false);
        t.start();
    }
}
