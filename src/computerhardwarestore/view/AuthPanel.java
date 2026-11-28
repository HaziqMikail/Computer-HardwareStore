package computerhardwarestore.view;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.User;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Login and sign-up screen. */
public class AuthPanel extends JPanel {
    private final AppContext ctx;
    private final Consumer<User> onLoggedIn;
    private final CardLayout cards = new CardLayout();
    private final JPanel body = new JPanel(cards);

    private final JTextField loginUser = new JTextField(20);
    private final JPasswordField loginPass = new JPasswordField(20);

    private final JTextField firstName = new JTextField(20);
    private final JTextField lastName = new JTextField(20);
    private final JComboBox<String> day = new JComboBox<>();
    private final JComboBox<String> month = new JComboBox<>();
    private final JComboBox<String> year = new JComboBox<>();
    private final JTextField regUser = new JTextField(20);
    private final JPasswordField regPass = new JPasswordField(20);
    private final JPasswordField regRepeat = new JPasswordField(20);
    private final JCheckBox terms = new JCheckBox("I accept the Terms and Conditions");

    public AuthPanel(AppContext ctx, Consumer<User> onLoggedIn) {
        super(new BorderLayout(10, 10));
        this.ctx = ctx;
        this.onLoggedIn = onLoggedIn;
        setBackground(Theme.LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel head = Theme.panel(new BorderLayout());
        head.add(Theme.title("Welcome to CodeFusion"), BorderLayout.CENTER);
        JLabel sub = new JLabel("Computer & Hardware Store", JLabel.CENTER);
        sub.setFont(Theme.HEADING);
        sub.setForeground(Theme.DARK);
        head.add(sub, BorderLayout.SOUTH);
        add(head, BorderLayout.NORTH);

        JPanel tabs = Theme.panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton loginTab = Theme.button("Login");
        JButton registerTab = Theme.button("Register");
        tabs.add(loginTab);
        tabs.add(registerTab);

        body.setBackground(Theme.LIGHT);
        body.add(buildLogin(), "login");
        body.add(buildRegister(), "register");
        loginTab.addActionListener(e -> cards.show(body, "login"));
        registerTab.addActionListener(e -> cards.show(body, "register"));

        JPanel center = Theme.panel(new BorderLayout(0, 10));
        center.add(tabs, BorderLayout.NORTH);
        center.add(body, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JButton exit = Theme.button("Exit");
        exit.addActionListener(e -> System.exit(0));
        JPanel south = Theme.panel(new FlowLayout(FlowLayout.LEFT));
        south.add(exit);
        add(south, BorderLayout.SOUTH);
    }

    public void reset() {
        loginUser.setText("");
        loginPass.setText("");
        cards.show(body, "login");
    }

    private JPanel buildLogin() {
        JPanel form = formPanel();
        GridBagConstraints g = constraints();
        addRow(form, g, 0, "Username:", loginUser);
        addRow(form, g, 1, "Password:", loginPass);
        JButton go = Theme.button("Login");
        go.setPreferredSize(new Dimension(160, 36));
        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.CENTER;
        form.add(go, g);
        go.addActionListener(e -> doLogin());
        loginPass.addActionListener(e -> doLogin());
        return wrap(form);
    }

    private JPanel buildRegister() {
        day.addItem("Day");
        for (int i = 1; i <= 31; i++) day.addItem(String.valueOf(i));
        month.addItem("Month");
        for (String m : new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"}) {
            month.addItem(m);
        }
        year.addItem("Year");
        for (int y = LocalDate.now().getYear(); y >= 1930; y--) year.addItem(String.valueOf(y));

        JPanel form = formPanel();
        GridBagConstraints g = constraints();
        addRow(form, g, 0, "First name:", firstName);
        addRow(form, g, 1, "Last name:", lastName);

        JPanel birth = Theme.panel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        birth.add(day);
        birth.add(month);
        birth.add(year);
        addRow(form, g, 2, "Birthdate:", birth);

        addRow(form, g, 3, "Username:", regUser);
        addRow(form, g, 4, "Password:", regPass);
        addRow(form, g, 5, "Repeat password:", regRepeat);

        terms.setOpaque(false);
        g.gridx = 0;
        g.gridy = 6;
        g.gridwidth = 2;
        form.add(terms, g);

        JButton go = Theme.button("Create account");
        go.setPreferredSize(new Dimension(180, 36));
        g.gridy = 7;
        g.anchor = GridBagConstraints.CENTER;
        form.add(go, g);
        go.addActionListener(e -> doRegister());
        return wrap(form);
    }

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
            cards.show(body, "login");
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

    private static JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.CREAM);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 3), BorderFactory.createEmptyBorder(20, 30, 20, 30)));
        return p;
    }

    private static GridBagConstraints constraints() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        return g;
    }

    private static void addRow(JPanel form, GridBagConstraints g, int row, String label, java.awt.Component field) {
        g.gridy = row;
        g.gridwidth = 1;
        g.gridx = 0;
        g.weightx = 0;
        JLabel l = new JLabel(label);
        l.setForeground(Theme.DARK);
        l.setFont(Theme.BODY_BOLD);
        form.add(l, g);
        g.gridx = 1;
        g.weightx = 1;
        form.add(field, g);
    }

    private static JPanel wrap(JPanel form) {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.LIGHT);
        outer.add(form);
        return outer;
    }
}
