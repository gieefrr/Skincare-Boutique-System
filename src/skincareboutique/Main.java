package skincareboutique;

import data.DataStore;
import gui.LoginFrame;
import gui.Theme;
/**
 * RADIANT GLOW - Skincare Boutique System
 * applies the theme, loads the saved data, then opens the login form.
 */
public class Main {   // where the program starts
    public static void main(String[] args) {   // only one DataStore is made, every screen shares it
        Theme.applyGlobalTheme();
        DataStore store = new DataStore();
        store.loadAll();
        LoginFrame login = new LoginFrame(store);
        login.setVisible(true);
    }
}
