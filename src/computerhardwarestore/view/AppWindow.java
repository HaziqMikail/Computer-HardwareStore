package computerhardwarestore.view;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.User;
import computerhardwarestore.view.auth.AuthPanel;
import computerhardwarestore.view.shell.AdminShell;
import computerhardwarestore.view.shell.CustomerShell;
import computerhardwarestore.view.shell.Shell;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/** The one window. Shows the login screen, then the customer or admin shell depending on the user's role. */
public class AppWindow extends JFrame {
    private final AppContext ctx;
    private final AuthPanel auth;
    private Shell shell;

    public AppWindow(AppContext ctx) {
        super("Computer & Hardware Store");
        this.ctx = ctx;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1180, 780);
        java.net.URL logo = AppWindow.class.getClassLoader().getResource("images/back5.png");
        if (logo != null) setIconImage(new ImageIcon(logo).getImage());

        auth = new AuthPanel(ctx, this::onLoggedIn);
        setLayout(new BorderLayout());
        add(auth, BorderLayout.CENTER);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, Math.max(0, (screen.height - getHeight()) / 2));
    }

    private void onLoggedIn(User user) {
        shell = user.isAdmin() ? new AdminShell(ctx, user, this::logout) : new CustomerShell(ctx, user, this::logout);
        swap(shell.component());
        shell.start();
    }

    private void logout() {
        ctx.auth.logout();
        ctx.cart.clear();
        shell = null;
        auth.reset();
        swap(auth);
    }

    private void swap(java.awt.Component c) {
        getContentPane().removeAll();
        getContentPane().add(c, BorderLayout.CENTER);
        getContentPane().revalidate();
        getContentPane().repaint();
    }
}
