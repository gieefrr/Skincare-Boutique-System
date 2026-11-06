package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import data.DataStore;
import model.User;

/**
 * USER ACCOUNTS TAB (Admin only)
 * - Add a new system user and choose the access level (Admin or Staff).
 * - Block / unblock accounts (users are blocked after 3 wrong logins).
 * - Delete accounts (needs the admin password).
 * Only the admin can create accounts, so only the boutique's staff can use the system.
 */
// [INHERITANCE] UserPanel extends JPanel
// [ABSTRACTION] implements the ActionListener interface
public class UserPanel extends JPanel implements ActionListener {

    private DataStore store;
    private User currentUser;

    private DefaultTableModel tableModel;
    private JTable table;

    private JButton addButton;
    private JButton blockButton;
    private JButton deleteButton;

    public UserPanel(DataStore store, User currentUser) {   // only made for admins, see MainFrame.createTabs()
        this.store = store;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Theme.BACKGROUND);

        String[] columns = {"No.", "Username", "Access Level", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        Theme.styleTable(table);

        addButton = Theme.createButton("Add User", "Add a new system user (Alt+A)", KeyEvent.VK_A);
        blockButton = Theme.createButton("Block / Unblock", "Block or unblock the selected user (Alt+B)", KeyEvent.VK_B);
        deleteButton = Theme.createButton("Delete User", "Delete the selected user - needs admin password (Alt+D)", KeyEvent.VK_D);
        addButton.addActionListener(this);
        blockButton.addActionListener(this);
        deleteButton.addActionListener(this);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(blockButton);
        buttons.add(deleteButton);

        JPanel card = Theme.createCard();
        card.setLayout(new BorderLayout(8, 8));
        card.add(buttons, BorderLayout.NORTH);
        card.add(Theme.createScrollPane(table), BorderLayout.CENTER);

        add(Theme.createHeading("User Accounts"), BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
        refreshTable();
    }

    // [POLYMORPHISM - OVERRIDING] we write our own actionPerformed() for the ActionListener interface
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == addButton) {
            addUser();
        } else if (source == blockButton) {
            toggleBlock();
        } else if (source == deleteButton) {
            deleteUser();
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        ArrayList<User> users = store.getUsers();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Object[] row = {i + 1, user.getUsername(), user.getRole(), user.getStatus()};
            tableModel.addRow(row);
        }
    }

    private User getSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.showWarning(this, "Please select a user from the table first.");
            return null;
        }
        String username = (String) tableModel.getValueAt(row, 1);
        return store.findUser(username);
    }

    private void addUser() {
        SignUpDialog dialog = new SignUpDialog(null, store);
        dialog.setVisible(true);        // waits here until the dialog is closed
        if (dialog.isSaved()) {
            refreshTable();
        }
    }

    private void toggleBlock() {   // blocks or unblocks, but not your own account
        User user = getSelectedUser();
        if (user == null) {
            return;
        }
        if (user == currentUser) {
            Dialogs.showError(this, "You cannot block your own account.");
            return;
        }
        if (user.isBlocked()) {
            user.setStatus("Active");
            Dialogs.showInfo(this, "\"" + user.getUsername() + "\" is now unblocked and can log in again.");
        } else {
            user.setStatus("Blocked");
            Dialogs.showInfo(this, "\"" + user.getUsername() + "\" is now blocked.");
        }
        store.saveUsers();
        refreshTable();
    }

    private void deleteUser() {   // can't delete yourself or the last admin
        User user = getSelectedUser();
        if (user == null) {
            return;
        }
        if (user == currentUser) {
            Dialogs.showError(this, "You cannot delete your own account while logged in.");
            return;
        }
        // [POLYMORPHISM] canManageUsers() is true only for an Admin object
        if (user.canManageUsers() && store.countAdmins() == 1) {
            Dialogs.showError(this, "You cannot delete the last admin account.");
            return;
        }
        if (!Dialogs.confirm(this, "Delete the account \"" + user.getUsername() + "\"?")) {
            return;
        }
        if (!Dialogs.askAdminPassword(this, store)) {
            return;
        }
        store.deleteUser(user);
        refreshTable();
        Dialogs.showInfo(this, "The account was deleted.");
    }
}
