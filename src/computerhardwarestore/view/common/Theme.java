package computerhardwarestore.view.common;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

/** Design tokens (colours, fonts, spacing) and one-time FlatLaf setup. */
public final class Theme {
    public static final Color ESPRESSO = new Color(0x2B1B17);
    public static final Color ESPRESSO_SOFT = new Color(0x3E2A24);
    public static final Color GOLD = new Color(0xD9A441);
    public static final Color GOLD_DARK = new Color(0xB9852A);
    public static final Color BG = new Color(0xF6F2EC);
    public static final Color CARD = Color.WHITE;
    public static final Color LINE = new Color(0xE6DFD5);
    public static final Color TEXT = new Color(0x2B1B17);
    public static final Color MUTED = new Color(0x7A6B63);
    public static final Color SUCCESS = new Color(0x2E7D4F);
    public static final Color SUCCESS_BG = new Color(0xE3F3EA);
    public static final Color WARNING = new Color(0xB36B00);
    public static final Color WARNING_BG = new Color(0xFFF1D6);
    public static final Color DANGER = new Color(0xB3261E);
    public static final Color DANGER_BG = new Color(0xFBE4E2);

    public static final int ARC = 14;
    public static final int GAP = 16;

    public static final Font H1 = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font H2 = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private Theme() {}

    /** Call once, before creating any window. */
    public static void install() {
        UIManager.put("defaultFont", BODY);
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.accentColor", GOLD);
        UIManager.put("Component.focusColor", GOLD);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
        UIManager.put("Table.selectionBackground", new Color(0xF3E3BE));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Table.gridColor", LINE);
        UIManager.put("TableHeader.background", BG);
        UIManager.put("TableHeader.foreground", MUTED);
        UIManager.put("TableHeader.bottomSeparatorColor", LINE);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", BG);
        UIManager.put("Spinner.buttonStyle", "button");
        UIManager.put("TextField.margin", new java.awt.Insets(6, 10, 6, 10));
        UIManager.put("PasswordField.margin", new java.awt.Insets(6, 10, 6, 10));
        UIManager.put("ComboBox.padding", new java.awt.Insets(5, 8, 5, 8));
        FlatLightLaf.setup();
        // Must come after setup(): setup() installs the standard UI classes.
        UIManager.put("LabelUI", PaddedLabelUI.class.getName());
        UIManager.getDefaults().put(PaddedLabelUI.class.getName(), PaddedLabelUI.class);
    }
}
