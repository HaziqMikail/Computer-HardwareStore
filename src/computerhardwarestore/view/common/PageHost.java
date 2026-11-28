package computerhardwarestore.view.common;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;

/** Holds the pages of a shell and shows one at a time, refreshing it first if it is Refreshable. */
public class PageHost extends JPanel {
    private final CardLayout cards = new CardLayout();
    private final Map<String, JComponent> pages = new HashMap<>();

    public PageHost() {
        setLayout(cards);
        setBackground(Theme.BG);
    }

    public void add(String key, JComponent page) {
        pages.put(key, page);
        add(page, key);
    }

    public void show(String key) {
        JComponent page = pages.get(key);
        if (page instanceof Refreshable r) r.refresh();
        cards.show(this, key);
    }
}
