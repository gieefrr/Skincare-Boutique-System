package model;

/**
 * [INHERITANCE] an Admin "is a" User (Admin extends User).
 * The admin is the boutique manager: manages user accounts (add, block/unblock, delete)
 * and products (add, update, delete), and can also take orders.
 */
public class Admin extends User {   // the manager, can do everything

    public Admin(String username, String password) {   // used in SignUpDialog.register() and for the default admin account
        super(username, password);              // [INHERITANCE] calls the User constructor
    }

    public Admin(String username, String password, String status) {   // used when loading users.txt since the status is saved there
        super(username, password, status);      // [INHERITANCE] calls the User constructor
    }

    // [POLYMORPHISM - OVERRIDING] Admin gives its own answer to the abstract methods of User
    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public boolean canManageUsers() {   // this is why only the admin sees the User Accounts tab
        return true;
    }

    @Override
    public boolean canManageProducts() {   // admin gets the product form and the Void button
        return true;
    }
}
