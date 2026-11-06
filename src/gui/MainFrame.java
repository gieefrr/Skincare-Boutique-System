package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import data.DataStore;
import model.User;

/**
 * MAIN DASHBOARD shown after a successful login.
 * Tabs: Products, Orders & Sales, and User Accounts (admin only).
 *
 * ActionListener: actionPerformed() runs when a button is clicked.
 * ChangeListener: stateChanged() runs when the user switches to another tab.
 */
// [INHERITANCE] MainFrame extends JFrame (it is a window)
// [ABSTRACTION] implements the ActionListener and ChangeListener interfaces
public class MainFrame extends JFrame implements ActionListener, ChangeListener {

    private DataStore store;
    private User currentUser;

    private ProductPanel productPanel;
    private OrderPanel orderPanel;

    private JButton passwordButton;
    private JButton logoutButton;

    public MainFrame(DataStore store, User currentUser) {   // opened from LoginFrame after a correct password
        this.store = store;
        this.currentUser = currentUser;

        setTitle("Skincare Boutique System");
        setIconImage(LogoPanel.loadImage("icon.png"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // data is saved after every change
        setSize(1250, 740);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(createHeader(), BorderLayout.NORTH);
        add(createTabs(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.WHITE);
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 4, 0, Theme.PINK), new EmptyBorder(8, 20, 8, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(new LogoPanel("icon.png", 70 , 70));
        JLabel title = new JLabel("Radiant Glow");
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.ROSE);
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 18));
        right.setOpaque(false);
        JLabel userLabel = new JLabel("Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        userLabel.setFont(Theme.BUTTON_FONT);
        userLabel.setForeground(Theme.TEXT_DARK);
        userLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        passwordButton = Theme.createButton("Change Password", "Change your password (Alt+G)", KeyEvent.VK_G);
        logoutButton = Theme.createButton("Logout", "Log out of the system (Alt+L)", KeyEvent.VK_L);
        passwordButton.addActionListener(this);
        logoutButton.addActionListener(this);
        right.add(userLabel);
        right.add(passwordButton);
        right.add(logoutButton);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane createTabs() {   // the panels are made here
        JTabbedPane tabs = new JTabbedPane();
        productPanel = new ProductPanel(store, currentUser);
        orderPanel = new OrderPanel(store, currentUser);

        tabs.addTab("  Products  ", productPanel);
        tabs.setMnemonicAt(0, KeyEvent.VK_1);

        tabs.addTab("  Orders & Sales  ", orderPanel);
        tabs.setToolTipTextAt(1, "Take customer orders and view sales (Alt+2)");
        tabs.setMnemonicAt(1, KeyEvent.VK_2);

        // [POLYMORPHISM] canManageUsers() runs the Admin or Staff version:
        // only users that can manage users (Admin) will see this tab
        if (currentUser.canManageUsers()) {
            tabs.addTab("  User Accounts  ", new UserPanel(store, currentUser));
            tabs.setToolTipTextAt(2, "Add, block and delete system users (Alt+3)");
            tabs.setMnemonicAt(2, KeyEvent.VK_3);
        }

        tabs.addChangeListener(this);
        return tabs;
    }

    // [POLYMORPHISM - OVERRIDING] we write our own actionPerformed() for the ActionListener interface
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == passwordButton) {
            changePassword();
        } else if (source == logoutButton) {
            logout();
        }
    }

    // Runs every time a tab is opened, so it always shows the newest data
    @Override
    public void stateChanged(ChangeEvent e) {   // so the tabs always show the latest data
        productPanel.refreshTable();
        orderPanel.refreshAll();
    }

    private void changePassword() {   // has to match the current password first
        JPasswordField currentField = new JPasswordField(15);
        JPasswordField newField = new JPasswordField(15);
        JPasswordField confirmField = new JPasswordField(15);
        Object[] form = {
            "Current Password *", currentField,
            "New Password *", newField,
            "Confirm New Password *", confirmField,
            "(at least 6 characters with letters and numbers)"
        };
        int answer = JOptionPane.showConfirmDialog(this, form, "Change Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        String currentPassword = new String(currentField.getPassword());
        String newPassword = new String(newField.getPassword());
        if (!currentUser.checkPassword(currentPassword)) {
            Dialogs.showError(this, "Your current password is incorrect.");
            return;
        }
        String error = User.validatePassword(newPassword, new String(confirmField.getPassword()));
        if (!error.isEmpty()) {
            Dialogs.showError(this, error);
            return;
        }
        if (newPassword.equals(currentPassword)) {
            Dialogs.showError(this, "The new password must be different from the current one.");
            return;
        }
        currentUser.setPassword(newPassword);
        store.saveUsers();
        Dialogs.showInfo(this, "Your password was changed successfully.");
    }

    private void logout() {   // goes back to a new login form
        if (Dialogs.confirm(this, "Are you sure you want to log out?")) {
            dispose();
            LoginFrame login = new LoginFrame(store);
            login.setVisible(true);
        }
    }
}
