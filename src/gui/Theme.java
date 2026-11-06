package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;

/**
 * Holds the soft pink color scheme and helper methods that create styled components.
 * Every form uses these methods so the whole system looks the same.
 */
public class Theme {   // all static, we just call the methods we need

    // Constant variables (public static final) are written in UPPERCASE
    public static final Color BACKGROUND = new Color(255, 245, 247);  // very light pink - page background
    public static final Color PINK = new Color(255, 206, 216);        // #FFCED8 - outlines, table header
    public static final Color BLUSH = new Color(255, 221, 221);       // #FFDDDD - selected rows
    public static final Color ROSE = new Color(176, 62, 98);          // deep rose - buttons and titles
    public static final Color DARK_ROSE = new Color(140, 40, 75);     // button when pressed
    public static final Color BORDER = new Color(236, 150, 172);      // text field outlines
    public static final Color TEXT_DARK = new Color(74, 32, 48);      // normal text
    public static final Color WHITE = Color.WHITE;
    public static final Color ERROR_RED = new Color(198, 40, 40);

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);

    public static final String PESO = "₱";

    // Applies the pink look to the built-in Swing parts (message boxes, tabs, menus, tooltips)
    public static void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Could not apply look and feel: " + e.getMessage());
        }
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", BACKGROUND);
        UIManager.put("OptionPane.messageFont", NORMAL_FONT);
        UIManager.put("OptionPane.messageForeground", TEXT_DARK);
        UIManager.put("OptionPane.buttonFont", BUTTON_FONT);
        UIManager.put("Button.background", ROSE);
        UIManager.put("Button.foreground", WHITE);
        UIManager.put("Button.select", DARK_ROSE);
        UIManager.put("Label.font", NORMAL_FONT);
        UIManager.put("Label.foreground", TEXT_DARK);
        UIManager.put("TabbedPane.font", BUTTON_FONT);
        UIManager.put("TabbedPane.foreground", TEXT_DARK);
        UIManager.put("TabbedPane.background", PINK);
        UIManager.put("TabbedPane.selected", WHITE);
        UIManager.put("TabbedPane.contentAreaColor", BACKGROUND);
        UIManager.put("ToolTip.background", WHITE);
        UIManager.put("ToolTip.foreground", ROSE);
        UIManager.put("ToolTip.font", NORMAL_FONT);
        UIManager.put("ComboBox.font", NORMAL_FONT);
        UIManager.put("ComboBox.background", WHITE);
        UIManager.put("ComboBox.selectionBackground", BLUSH);
        UIManager.put("ComboBox.selectionForeground", TEXT_DARK);
        UIManager.put("MenuBar.background", WHITE);
        UIManager.put("Menu.font", NORMAL_FONT);
        UIManager.put("MenuItem.font", NORMAL_FONT);
        UIManager.put("Menu.selectionBackground", PINK);
        UIManager.put("MenuItem.selectionBackground", PINK);
        UIManager.put("TextField.font", NORMAL_FONT);
        UIManager.put("PasswordField.font", NORMAL_FONT);
        UIManager.put("Spinner.font", NORMAL_FONT);
        UIManager.put("CheckBox.font", NORMAL_FONT);
    }

    // Creates a rose button with white text and a tooltip
    public static JButton createButton(String text, String toolTip) {
        JButton button = new JButton(text);
        button.setBackground(ROSE);
        button.setForeground(WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(toolTip);
        return button;
    }

    // [POLYMORPHISM - OVERLOADING] same name, but this version also adds an Alt + key shortcut
    public static JButton createButton(String text, String toolTip, int shortcutKey) {
        JButton button = createButton(text, toolTip);
        button.setMnemonic(shortcutKey);
        return button;
    }

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(NORMAL_FONT);
        label.setForeground(TEXT_DARK);
        return label;
    }

    public static JLabel createHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADER_FONT);
        label.setForeground(ROSE);
        return label;
    }

    // Small red label shown under a text field when the input is wrong (empty = hidden)
    public static JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(SMALL_FONT);
        label.setForeground(ERROR_RED);
        return label;
    }

    public static JTextField createTextField(String toolTip) {
        JTextField field = new JTextField(15);
        field.setFont(NORMAL_FONT);
        field.setToolTipText(toolTip);
        markField(field, false);
        return field;
    }

    public static JPasswordField createPasswordField(String toolTip) {
        JPasswordField field = new JPasswordField(15);
        field.setFont(NORMAL_FONT);
        field.setToolTipText(toolTip);
        markField(field, false);
        return field;
    }

    // Red border = required/invalid field, pink border = OK
    public static void markField(JComponent field, boolean hasError) {
        Color color = BORDER;
        if (hasError) {
            color = ERROR_RED;
        }
        field.setBorder(new CompoundBorder(new LineBorder(color, 2, true), new EmptyBorder(4, 6, 4, 6)));
    }

    // White "card" panel with a pink outline, used to group form contents
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(PINK, 2, true), new EmptyBorder(15, 15, 15, 15)));
        return card;
    }

    public static void styleTable(JTable table) {
        table.setFont(NORMAL_FONT);
        table.setForeground(TEXT_DARK);
        table.setRowHeight(28);
        table.setSelectionBackground(BLUSH);
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(PINK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null);     // cells cannot be edited by typing

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBackground(PINK);
        header.setForeground(TEXT_DARK);
        header.setFont(BUTTON_FONT);
    }

    public static JScrollPane createScrollPane(Component content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.getViewport().setBackground(WHITE);
        scrollPane.setBorder(new LineBorder(PINK, 2));
        return scrollPane;
    }

    // Formats an amount of money, example: 1250.5 becomes "₱1,250.50"
    public static String money(double amount) {
        return PESO + String.format("%,.2f", amount);
    }
}
