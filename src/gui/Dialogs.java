package gui;

import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import data.DataStore;

/**
 * Short helper methods for the message boxes used everywhere in the system.
 */
public class Dialogs {   // shorter than typing JOptionPane every time
    private static final String TITLE = "Radiant Glow";
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, TITLE, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, TITLE, JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, TITLE + " - Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {   // true if they clicked Yes
        int answer = JOptionPane.showConfirmDialog(parent, message, "Please Confirm", JOptionPane.YES_NO_OPTION);
        return answer == JOptionPane.YES_OPTION;
    }

    // Security: asks for an ADMIN password before a delete. Returns true only if it is correct.
    public static boolean askAdminPassword(Component parent, DataStore store) {
        JPasswordField passwordField = new JPasswordField(15);
        Object[] message = {"Enter an ADMIN password to continue:", passwordField};
        int answer = JOptionPane.showConfirmDialog(parent, message, "Admin Verification",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return false;
        }
        if (store.isAdminPassword(new String(passwordField.getPassword()))) {
            return true;
        }
        showError(parent, "Incorrect admin password. The action was cancelled.");
        return false;
    }
}
