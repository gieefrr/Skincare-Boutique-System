package model;

/**
 * [ABSTRACTION] User is an ABSTRACT CLASS - a general system user. You cannot create
 * a "User" object directly, only its subclasses Admin and Staff.
 * [ENCAPSULATION] all fields are private and accessed through getters and setters.
 */
public abstract class User {   // Admin and Staff both extend this

    // [ENCAPSULATION] private fields: other classes cannot touch them directly
    private String username;
    private String password;
    private String status;      // "Active" or "Blocked"

    // [POLYMORPHISM - OVERLOADING] two constructors with different parameters.
    // A new user is Active by default.
    public User(String username, String password) {   // Admin and Staff call this for new accounts, so they start as Active
        this(username, password, "Active");     // calls the other constructor
    }

    public User(String username, String password, String status) {   // used when the status is already known, like when loading users.txt
        this.username = username;
        this.password = password;
        this.status = status;
    }

    // [ABSTRACTION] abstract methods: no body here, every subclass must override them
    public abstract String getRole();

    public abstract boolean canManageUsers();

    public abstract boolean canManageProducts();

    // [ENCAPSULATION] getters (accessors)
    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    // [ENCAPSULATION] setters (mutators)
    public void setPassword(String password) {
        this.password = password;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // [ENCAPSULATION] data hiding: the password has no getter, it can only be checked
    public boolean checkPassword(String input) {
        return password.equals(input);
    }

    public boolean isBlocked() {   // LoginFrame checks this before the password
        return status.equals("Blocked");
    }

    // One line in users.txt, example: Admin|admin|admin123|Active
    // [POLYMORPHISM] getRole() runs the Admin or Staff version
    public String toFileString() {
        return getRole() + "|" + username + "|" + password + "|" + status;
    }

    // Username rules: 4 to 20 characters, only letters, numbers or _
    // Returns an error message, or an empty String "" if the username is OK.
    public static String validateUsername(String username) {
        if (username.length() < 4 || username.length() > 20) {
            return "Username must be 4 to 20 characters long.";
        }
        for (int i = 0; i < username.length(); i++) {
            char c = username.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return "Username can only have letters, numbers or _";
            }
        }
        return "";
    }

    // Password security rules used by Sign Up and Change Password.
    // Returns an error message, or an empty String "" if the password is OK.
    public static String validatePassword(String password) {
        if (password.length() < 6) {
            return "Password must be at least 6 characters long.";
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            return "Password must contain both letters and numbers.";
        }
        if (password.contains("|")) {
            return "Password must not contain the | character.";
        }
        return "";      // no error
    }

    // [POLYMORPHISM - OVERLOADING] same name, but this one also checks the confirm password
    public static String validatePassword(String password, String confirm) {
        String error = validatePassword(password);
        if (!error.isEmpty()) {
            return error;
        }
        if (!password.equals(confirm)) {
            return "Passwords do not match. Please re-type your password.";
        }
        return "";
    }
}
