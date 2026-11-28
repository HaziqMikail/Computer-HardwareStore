package computerhardwarestore.view;

import computerhardwarestore.exception.AppException;
import computerhardwarestore.exception.DataAccessException;
import computerhardwarestore.exception.InvalidLoginException;
import computerhardwarestore.exception.OutOfStockException;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/** Turns exceptions into friendly dialogs. Users never see a stack trace; unexpected errors are only logged. */
public final class Dialogs {
    private static final Logger LOG = Logger.getLogger(Dialogs.class.getName());

    private Dialogs() {}

    public static void error(Component parent, Throwable t) {
        if (t instanceof AppException) {
            String title = switch (t) {
                case InvalidLoginException e -> "Login failed";
                case OutOfStockException e -> "Not enough stock";
                case DataAccessException e -> "Database problem";
                default -> "Please check your input";
            };
            if (t.getCause() != null) {
                LOG.log(Level.WARNING, t.getMessage(), t.getCause());
            }
            JOptionPane.showMessageDialog(parent, t.getMessage(), title, JOptionPane.WARNING_MESSAGE);
        } else {
            LOG.log(Level.SEVERE, "Unexpected error", t);
            JOptionPane.showMessageDialog(parent,
                    "Something unexpected went wrong. Please try again.\nIf it keeps happening, restart the application.",
                    "Unexpected error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "CodeFusion", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Please check", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
