package computerhardwarestore;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.dao.jdbc.DatabaseManager;
import computerhardwarestore.view.AppWindow;
import computerhardwarestore.view.common.Theme;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Application entry point: prepares the database, then opens the main window. */
public class ComputerHardwareStore {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            try {
                DatabaseManager.getInstance().initialise();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        e.getMessage() + "\n\nSee README.md (Database setup) for how to configure PostgreSQL.",
                        "Cannot start the store", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }
            new AppWindow(new AppContext()).setVisible(true);
        });
    }
}
