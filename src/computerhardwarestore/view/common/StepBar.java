package computerhardwarestore.view.common;

import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** "1 Cart > 2 Payment > 3 Done" progress indicator for the buying flow. */
public class StepBar extends JPanel {
    private static final String[] STEPS = {"Cart", "Payment", "Done"};

    public StepBar(int active) {
        super(new FlowLayout(FlowLayout.LEFT, 10, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.GAP, 0));
        for (int i = 0; i < STEPS.length; i++) {
            boolean done = i < active;
            boolean current = i == active;
            JLabel dot = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            dot.setOpaque(true);
            dot.setFont(Theme.BOLD);
            dot.setPreferredSize(new java.awt.Dimension(28, 28));
            dot.setBackground(done || current ? Theme.GOLD : new Color(0xE9E4DC));
            dot.setForeground(done || current ? Theme.ESPRESSO : Theme.MUTED);
            dot.putClientProperty("FlatLaf.style", "arc: 999");
            add(dot);
            JLabel name = new JLabel(STEPS[i]);
            name.setFont(current ? Theme.BOLD : Theme.BODY);
            name.setForeground(current ? Theme.TEXT : Theme.MUTED);
            add(name);
            if (i < STEPS.length - 1) {
                JLabel sep = new JLabel("—");
                sep.setForeground(Theme.LINE);
                add(sep);
            }
        }
    }
}
