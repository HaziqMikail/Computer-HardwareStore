package computerhardwarestore.view.auth;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.User;
import computerhardwarestore.view.common.Dialogs;
import computerhardwarestore.view.common.Theme;
import computerhardwarestore.view.common.Ui;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Login / sign-up: a brand panel on the left and the form on the right. */
public class AuthPanel extends JPanel {
    private final AppContext ctx;
    private final Consumer<User> onLoggedIn;
    private final CardLayout cards = new CardLayout();
    private final JPanel forms = new JPanel(cards);

    private final JTextField loginUser = field();
    private final JPasswordField loginPass = password();

    private final JTextField firstName = field();
    private final JTextField lastName = field();
    private final JComboBox<String> day = new JComboBox<>();
    private final JComboBox<String> month = new JComboBox<>();
    private final JComboBox<String> year = new JComboBox<>();
    private final JTextField regUser = field();
    private final JPasswordField regPass = password();
    private final JPasswordField regRepeat = password();
    private final JCheckBox terms = new JCheckBox("I accept the Terms and Conditions");

    public AuthPanel(AppContext ctx, Consumer<User> onLoggedIn) {
        super(new GridLayout(1, 2));
        this.ctx = ctx;
        this.onLoggedIn = onLoggedIn;
        add(brandPanel());

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.BG);
        forms.setOpaque(false);
        forms.add(centered(loginForm()), "login");
        forms.add(centered(registerForm()), "register");
        right.setLayout(new BorderLayout());
        right.add(forms, BorderLayout.CENTER);
        add(right);
    }

    public void reset() {
        loginUser.setText("");
        loginPass.setText("");
        cards.show(forms, "login");
    }

    // ---------- left brand panel ----------

    private JPanel brandPanel() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Theme.ESPRESSO, 0, getHeight(), new Color(0x4A2E26)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(217, 164, 65, 30));
                g2.fillOval(-120, getHeight() - 260, 420, 420);
                g2.fillOval(getWidth() - 180, -140, 320, 320);
                g2.dispose();
            }
        };
        JPanel box = Ui.transparent(null);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        java.net.URL logo = AuthPanel.class.getClassLoader().getResource("images/back5.png");
        if (logo != null) {
            JLabel img = new JLabel(new ImageIcon(new ImageIcon(logo).getImage()
                    .getScaledInstance(72, 72, java.awt.Image.SCALE_SMOOTH)));
            img.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(img);
            box.add(Box.createVerticalStrut(18));
        }
        JLabel name = new JLabel("<html>Computer &amp;<br/>Hardware</html>");
        name.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 40));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(name);
        JLabel tag = new JLabel("<html>Computers, keyboards, headsets<br/>and every part in between.</html>");
        tag.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 18));
        tag.setForeground(Theme.GOLD);
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(Box.createVerticalStrut(10));
        box.add(tag);
        box.add(Box.createVerticalStrut(34));
        for (String s : new String[]{"Live stock on every product", "Simple cart and checkout",
                "Receipts you can save or print"}) {
            JLabel l = new JLabel("•  " + s);
            l.setFont(Theme.BODY);
            l.setForeground(new Color(0xEADFD2));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(l);
            box.add(Box.createVerticalStrut(8));
        }
        p.add(box);
        return p;
    }

    // ---------- forms ----------

    private JPanel loginForm() {
        JPanel f = formBox(380);
        f.add(Ui.h1("Welcome back"));
        f.add(Box.createVerticalStrut(4));
        f.add(Ui.muted("Log in to continue shopping."));
        f.add(Box.createVerticalStrut(24));
        f.add(labelled("Username", loginUser));
        f.add(Box.createVerticalStrut(14));
        f.add(labelled("Password", withReveal(loginPass)));
        f.add(Box.createVerticalStrut(24));
        JButton go = Ui.primary("Log in");
        full(go);
        f.add(go);
        f.add(Box.createVerticalStrut(18));
        JButton toRegister = Ui.link("New here? Create an account");
        toRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.add(toRegister);

        go.addActionListener(e -> doLogin());
        loginPass.addActionListener(e -> doLogin());
        toRegister.addActionListener(e -> cards.show(forms, "register"));
        return f;
    }

    private JPanel registerForm() {
        day.addItem("Day");
        for (int i = 1; i <= 31; i++) day.addItem(String.valueOf(i));
        month.addItem("Month");
        for (String m : new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"}) {
            month.addItem(m);
        }
        year.addItem("Year");
        for (int y = LocalDate.now().getYear(); y >= 1930; y--) year.addItem(String.valueOf(y));

        JPanel f = formBox(420);
        f.add(Ui.h1("Create account"));
        f.add(Box.createVerticalStrut(4));
        f.add(Ui.muted("It only takes a minute."));
        f.add(Box.createVerticalStrut(18));

        JPanel names = new JPanel(new GridLayout(1, 2, 12, 0));
        names.setOpaque(false);
        names.add(labelled("First name", firstName));
        names.add(labelled("Last name", lastName));
        names.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.add(names);
        f.add(Box.createVerticalStrut(12));

        JPanel birth = new JPanel(new GridLayout(1, 3, 8, 0));
        birth.setOpaque(false);
        birth.add(day);
        birth.add(month);
        birth.add(year);
        f.add(labelled("Birthdate", birth));
        f.add(Box.createVerticalStrut(12));
        f.add(labelled("Username", regUser));
        f.add(Box.createVerticalStrut(12));
        f.add(labelled("Password", withReveal(regPass)));
        f.add(Ui.muted("8+ characters with upper, lower, number and symbol"));
        f.add(Box.createVerticalStrut(12));
        f.add(labelled("Repeat password", regRepeat));
        f.add(Box.createVerticalStrut(12));
        terms.setOpaque(false);
        terms.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.add(terms);
        f.add(Box.createVerticalStrut(16));
        JButton go = Ui.primary("Create account");
        full(go);
        f.add(go);
        f.add(Box.createVerticalStrut(14));
        JButton toLogin = Ui.link("Already registered? Log in");
        toLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.add(toLogin);

        go.addActionListener(e -> doRegister());
        toLogin.addActionListener(e -> cards.show(forms, "login"));
        return f;
    }

    // ---------- actions ----------

    private void doLogin() {
        try {
            User user = ctx.auth.login(loginUser.getText(), loginPass.getPassword());
            loginPass.setText("");
            onLoggedIn.accept(user);
        } catch (Exception ex) {
            Dialogs.error(this, ex);
            loginPass.setText("");
        }
    }

    private void doRegister() {
        try {
            ctx.auth.register(firstName.getText(), lastName.getText(),
                    (String) day.getSelectedItem(), (String) month.getSelectedItem(), (String) year.getSelectedItem(),
                    regUser.getText(), regPass.getPassword(), regRepeat.getPassword(), terms.isSelected());
            Dialogs.info(this, "Registration successful! Please log in.");
            loginUser.setText(regUser.getText().trim());
            clearRegister();
            cards.show(forms, "login");
            loginPass.requestFocusInWindow();
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private void clearRegister() {
        firstName.setText("");
        lastName.setText("");
        regUser.setText("");
        regPass.setText("");
        regRepeat.setText("");
        day.setSelectedIndex(0);
        month.setSelectedIndex(0);
        year.setSelectedIndex(0);
        terms.setSelected(false);
    }

    // ---------- helpers ----------

    private static JPanel centered(JPanel form) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(form);
        return p;
    }

    private static JTextField field() {
        JTextField t = new JTextField();
        t.putClientProperty("JTextField.placeholderText", "");
        return t;
    }

    private static JPasswordField password() {
        JPasswordField p = new JPasswordField();
        p.putClientProperty("JPasswordField.showRevealButton", true);
        return p;
    }

    private static JComponent withReveal(JPasswordField p) {
        return p;
    }

    private static JPanel formBox(int width) {
        JPanel f = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(width + 60, super.getPreferredSize().height);
            }
        };
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        return f;
    }

    private static JPanel labelled(String label, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.add(Ui.bold(label), BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 10));
        return p;
    }

    private static void full(JButton b) {
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    }
}
