package gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import data.DataStore;
import model.Admin;
import model.Staff;
import model.User;

/**
 * SIGN UP FORM (with password verification)
 * - Opened by the admin from the User Accounts tab ("Add User").
 * - The admin chooses the access level of the new user: Admin or Staff.
 * - Errors are shown right away under each field while the admin types (KeyListener).
 */
// [INHERITANCE] SignUpDialog extends JDialog (it is a pop-up window)
// [ABSTRACTION] implements the ActionListener and KeyListener interfaces
public class SignUpDialog extends JDialog implements ActionListener, KeyListener {

    private DataStore store;
    private boolean saved = false;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JComboBox<String> roleBox;

    // red messages under each field
    private JLabel usernameError;
    private JLabel passwordError;
    private JLabel confirmError;

    private JButton saveButton;
    private JButton cancelButton;

    public SignUpDialog(Window owner, DataStore store) {   // opened from UserPanel.addUser()
        super(owner, "Add New User", ModalityType.APPLICATION_MODAL);
        this.store = store;

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(15, 15, 15, 15));
        content.add(createForm(), BorderLayout.CENTER);
        setContentPane(content);

        pack();
        setSize(440, getHeight());      // wide enough for the error messages
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private JPanel createForm() {
        JPanel card = Theme.createCard();
        card.setLayout(new GridBagLayout());

        usernameField = Theme.createTextField("4-20 characters: letters, numbers or _");
        passwordField = Theme.createPasswordField("At least 6 characters with letters and numbers");
        confirmField = Theme.createPasswordField("Type the same password again");
        roleBox = new JComboBox<>(new String[]{"Staff", "Admin"});
        roleBox.setToolTipText("Access level of the new user");

        usernameError = Theme.createErrorLabel();
        passwordError = Theme.createErrorLabel();
        confirmError = Theme.createErrorLabel();

        // check the form every time a key is typed
        usernameField.addKeyListener(this);
        passwordField.addKeyListener(this);
        confirmField.addKeyListener(this);

        saveButton = Theme.createButton("Add User", "Save the account (Enter)", KeyEvent.VK_A);
        cancelButton = Theme.createButton("Cancel", "Close this form (Alt+C)", KeyEvent.VK_C);
        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);
        getRootPane().setDefaultButton(saveButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 4, 2, 4);

        card.add(Theme.createHeading("Add a New System User"), gbc);
        addField(card, gbc, 1, "Username *", usernameField, usernameError);
        addField(card, gbc, 4, "Password *", passwordField, passwordError);
        addField(card, gbc, 7, "Confirm Password *", confirmField, confirmError);
        gbc.gridy = 10;
        card.add(Theme.createLabel("Access Level *"), gbc);
        gbc.gridy = 11;
        card.add(roleBox, gbc);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        buttons.add(saveButton);
        buttons.add(cancelButton);
        gbc.gridy = 12;
        gbc.insets = new Insets(12, 4, 4, 4);
        card.add(buttons, gbc);
        return card;
    }

    // Adds 3 rows: the label, the input box, and the red error message under it
    private void addField(JPanel card, GridBagConstraints gbc, int row, String label,
                          JComponent field, JLabel error) {
        gbc.gridy = row;
        card.add(Theme.createLabel(label), gbc);
        gbc.gridy = row + 1;
        card.add(field, gbc);
        gbc.gridy = row + 2;
        card.add(error, gbc);
    }

    // [POLYMORPHISM - OVERRIDING] we write our own actionPerformed() for the ActionListener interface
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveButton) {
            register();
        } else if (e.getSource() == cancelButton) {
            dispose();
        }
    }

    // KeyListener has 3 methods. We only need keyReleased (after the letter is typed).
    @Override
    public void keyReleased(KeyEvent e) {
        checkForm(false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    /**
     * Checks every field and shows a red message under the wrong ones.
     * While typing (submitting = false), empty fields are not marked yet.
     * When Add User is clicked (submitting = true), empty fields are marked as required.
     * Returns true if everything is correct.
     */
    private boolean checkForm(boolean submitting) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        // --- username ---
        String error = "";
        if (username.isEmpty()) {
            if (submitting) {
                error = "Username is required.";
            }
        } else {
            error = User.validateUsername(username);
            if (error.isEmpty() && store.findUser(username) != null) {
                error = "This username is already taken.";
            }
        }
        boolean allCorrect = showError(usernameField, usernameError, error);

        // --- password ---
        error = "";
        if (password.isEmpty()) {
            if (submitting) {
                error = "Password is required.";
            }
        } else {
            error = User.validatePassword(password);
        }
        if (!showError(passwordField, passwordError, error)) {
            allCorrect = false;
        }

        // --- confirm password ---
        error = "";
        if (confirm.isEmpty()) {
            if (submitting) {
                error = "Please type the password again.";
            }
        } else if (!confirm.equals(password)) {
            error = "Passwords do not match.";
        }
        if (!showError(confirmField, confirmError, error)) {
            allCorrect = false;
        }

        // an empty field is not correct, even if no message is shown yet
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            allCorrect = false;
        }
        return allCorrect;
    }

    // Shows the message under the field. Returns true if there is no error.
    private boolean showError(JComponent field, JLabel errorLabel, String message) {
        if (message.isEmpty()) {
            errorLabel.setText(" ");
            Theme.markField(field, false);
            return true;
        }
        errorLabel.setText(message);
        Theme.markField(field, true);
        return false;
    }

    private void register() {   // creates an Admin or Staff depending on the role picked
        if (!checkForm(true)) {
            return;     // the red messages already tell the admin what to fix
        }
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        User user;
        if (roleBox.getSelectedItem().equals("Admin")) {
            user = new Admin(username, password);
        } else {
            user = new Staff(username, password);
        }
        String error = store.addUser(user);
        if (!error.isEmpty()) {
            Dialogs.showError(this, error);
            return;
        }

        Dialogs.showInfo(this, "Account \"" + username + "\" was created with " + user.getRole() + " access.");
        saved = true;
        dispose();
    }

    public boolean isSaved() {   // UserPanel checks this to know if it should refresh
        return saved;
    }
}
