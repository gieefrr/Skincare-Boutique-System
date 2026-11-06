package model;

/**
 * [INHERITANCE] a Staff "is a" User (Staff extends User).
 * Staff can view product information and handle customer orders,
 * but cannot change products or user accounts.
 */
public class Staff extends User {   // can only view products and handle orders

    public Staff(String username, String password) {   // used in SignUpDialog.register() and for the default staff account
        super(username, password);              // [INHERITANCE] calls the User constructor
    }

    public Staff(String username, String password, String status) {   // used when loading users.txt since the status is saved there
        super(username, password, status);      // [INHERITANCE] calls the User constructor
    }

    // [POLYMORPHISM - OVERRIDING] Staff gives its own answer to the abstract methods of User
    @Override
    public String getRole() {
        return "Staff";
    }

    @Override
    public boolean canManageUsers() {   // no User Accounts tab for staff
        return false;
    }

    @Override
    public boolean canManageProducts() {   // no product form or Void button for staff
        return false;
    }
}
