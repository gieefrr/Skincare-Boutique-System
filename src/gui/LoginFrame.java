package gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import data.DataStore;
import model.User;

/**
 * LOGIN FORM
 * - Admin and Staff log in here (accounts are stored in users.txt).
 * - Only 3 tries are allowed. On the 3rd wrong try the account is blocked
 *   and the system closes. Only an admin can unblock the account.
 *
 * It "implements ActionListener", so actionPerformed() runs whenever a button is clicked.
 */
// [INHERITANCE] LoginFrame extends JFrame, so it IS a window and gets all of JFrame's methods
// [ABSTRACTION] implements the ActionListener interface to react to button clicks
public class LoginFrame extends JFrame implements ActionListener {

    private static final int MAX_ATTEMPTS = 3;

    private DataStore store;
    private int attemptsLeft = MAX_ATTEMPTS;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordBox;
    private JLabel errorLabel;
    private JButton loginButton;

    public LoginFrame(DataStore store) {   // opened from Main and again after logging out
        this.store = store;

        setTitle("Radiant Glow - Login");
        setIconImage(LogoPanel.loadImage("logo.png"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);      // the X button closes the system
        setSize(860, 520);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));

        add(createBrandPanel());
        add(createFormPanel());
    }

    // Left side: the Radiant Glow logo and a short tagline
    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        LogoPanel logo = new LogoPanel("logo.png", 380, 285);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Glow with confidence, every day.");
        tagline.setFont(Theme.HEADER_FONT);
        tagline.setForeground(Theme.ROSE);
        tagline.setBorder(new EmptyBorder(0, 6, 0, 6));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(logo);
        content.add(Box.createVerticalStrut(18));
        content.add(tagline);
        panel.add(content);
        return panel;
    }

    // Right side: the login form
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.WHITE);

        JLabel heading = new JLabel("Welcome Back!");
        heading.setFont(Theme.TITLE_FONT);
        heading.setForeground(Theme.ROSE);

        usernameField = Theme.createTextField("Enter your username");
        passwordField = Theme.createPasswordField("Enter your password");

        showPasswordBox = new JCheckBox("Show password");
        showPasswordBox.setBackground(Theme.WHITE);
        showPasswordBox.setForeground(Theme.TEXT_DARK);
        showPasswordBox.addActionListener(this);

        // hidden at first, it only shows a message after a wrong login
        errorLabel = Theme.createErrorLabel();

        loginButton = Theme.createButton("Login", "Log in to the system (Enter or Alt+L)", KeyEvent.VK_L);
        loginButton.addActionListener(this);
        getRootPane().setDefaultButton(loginButton);    // pressing Enter clicks Login

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 45, 4, 45);

        gbc.gridy = 0;
        panel.add(heading, gbc);
        gbc.gridy = 1;
        panel.add(Theme.createLabel("Please log in to continue.  (* required)"), gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(14, 45, 4, 45);
        panel.add(Theme.createLabel("Username *"), gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 45, 4, 45);
        panel.add(usernameField, gbc);
        gbc.gridy = 4;
        panel.add(Theme.createLabel("Password *"), gbc);
        gbc.gridy = 5;
        panel.add(passwordField, gbc);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 45, 0, 45);
        panel.add(errorLabel, gbc);
        gbc.gridy = 7;
        panel.add(showPasswordBox, gbc);
        gbc.gridy = 8;
        gbc.insets = new Insets(14, 45, 5, 45);
        panel.add(loginButton, gbc);
        gbc.gridy = 9;
        gbc.insets = new Insets(10, 45, 5, 45);
        panel.add(Theme.createLabel("No account? Ask the admin to create one for you."), gbc);
        return panel;
    }

    // Runs when a button or the checkbox is clicked. getSource() tells us which one.
    // [POLYMORPHISM - OVERRIDING] we write our own actionPerformed() for the ActionListener interface
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            login();
        } else if (e.getSource() == showPasswordBox) {
            if (showPasswordBox.isSelected()) {
                passwordField.setEchoChar((char) 0);    // show the letters
            } else {
                passwordField.setEchoChar('•');         // hide the letters again
            }
        }
    }

    private void login() {   // wrong tries are counted here, 3 max
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // required fields check (does not count as a failed try)
        Theme.markField(usernameField, username.isEmpty());
        Theme.markField(passwordField, password.isEmpty());
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter your username and password.");
            return;
        }

        User user = store.findUser(username);
        if (user != null && user.isBlocked()) {
            errorLabel.setText("This account is blocked. Please ask the admin to unblock it.");
            return;
        }

        if (user != null && user.checkPassword(password)) {
            Dialogs.showInfo(this, "Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ").");
            MainFrame mainFrame = new MainFrame(store, user);
            mainFrame.setVisible(true);
            dispose();      // close the login window
            return;
        }

        // wrong username or password
        attemptsLeft--;
        passwordField.setText("");
        Theme.markField(passwordField, true);

        if (attemptsLeft > 0) {
            errorLabel.setText("Incorrect username or password. Attempts left: " + attemptsLeft);
            return;
        }

        // no tries left. Admin accounts are not blocked so someone can always unblock users.
        // [POLYMORPHISM] canManageUsers() runs the Admin or Staff version
        if (user != null && !user.canManageUsers()) {
            user.setStatus("Blocked");
            store.saveUsers();
            Dialogs.showError(this, "Too many invalid attempts.\nThe account \"" + user.getUsername()
                    + "\" is now BLOCKED. Only the admin can unblock it.\nThe system will now close.");
        } else {
            Dialogs.showError(this, "Too many invalid attempts.\nThe system will now close.");
        }
        System.exit(0);
    }
}
