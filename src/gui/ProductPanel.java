package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import data.DataStore;
import data.SortSearch;
import model.Product;
import model.User;

/**
 * PRODUCTS TAB
 * - Admin only: create, update and delete products (delete needs the admin password, with Undo).
 * - Everyone (Admin and Staff): view product information.
 * - Linear search by keyword, binary search by ID.
 * - Sort with bubble sort, selection sort or insertion sort.
 *
 * ActionListener: actionPerformed() runs when a button is clicked.
 * ListSelectionListener: valueChanged() runs when a row in the table is clicked.
 */
// [INHERITANCE] ProductPanel extends JPanel (it is a panel that can hold components)
// [ABSTRACTION] implements the ActionListener and ListSelectionListener interfaces
public class ProductPanel extends JPanel implements ActionListener, ListSelectionListener {

    private static final String[] COLUMNS = {"ID", "Category", "Product Name", "Brand", "Price", "Stock", "Best For"};
    private static final String[] SORT_OPTIONS = {
        "ID (Insertion Sort)",
        "Price: Low to High (Bubble Sort)",
        "Name: A to Z (Selection Sort)"
    };

    private DataStore store;
    private User currentUser;
    private ArrayList<Product> currentView = new ArrayList<>();   // products shown in the table

    // form fields
    private JTextField idField;
    private JComboBox<String> categoryBox;
    private JTextField nameField;
    private JTextField brandField;
    private JTextField priceField;
    private JTextField stockField;

    // search and sort
    private JTextField searchField;
    private JTextField findIdField;
    private JComboBox<String> sortBox;

    // buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton undoButton;
    private JButton infoButton;
    private JButton searchButton;
    private JButton findButton;
    private JButton sortButton;
    private JButton showAllButton;

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;

    public ProductPanel(DataStore store, User currentUser) {   // made in MainFrame.createTabs()
        this.store = store;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Theme.BACKGROUND);

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        // [POLYMORPHISM] canManageProducts() runs the Admin or Staff version:
        // only the admin (manager) sees the form for adding, editing and deleting products
        if (currentUser.canManageProducts()) {
            add(Theme.createHeading("Product Management"), BorderLayout.NORTH);
            add(formPanel, BorderLayout.WEST);
        } else {
            add(Theme.createHeading("Products"), BorderLayout.NORTH);
        }
        add(tablePanel, BorderLayout.CENTER);

        refreshTable();
        clearForm();
    }

    // ===================== LAYOUT =====================

    private JPanel createFormPanel() {
        JPanel card = Theme.createCard();
        card.setLayout(new GridBagLayout());

        idField = Theme.createTextField("Product IDs are numbered automatically");
        idField.setEditable(false);
        categoryBox = new JComboBox<>(Product.CATEGORIES);
        categoryBox.setToolTipText("Type of skincare product");
        nameField = Theme.createTextField("Example: Hydrating Toner");
        brandField = Theme.createTextField("Example: Cetaphil");
        priceField = Theme.createTextField("Price in pesos, example: 499.75");
        stockField = Theme.createTextField("Number of items available, example: 20");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 2, 5, 6);

        // heading across both columns
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(Theme.createHeading("Product Details"), gbc);
        gbc.gridwidth = 1;

        // one row per field: label on the left, input on the right
        addFormRow(card, gbc, 1, "Product ID", idField);
        addFormRow(card, gbc, 2, "Category *", categoryBox);
        addFormRow(card, gbc, 3, "Name *", nameField);
        addFormRow(card, gbc, 4, "Brand *", brandField);
        addFormRow(card, gbc, 5, "Price (" + Theme.PESO + ") *", priceField);
        addFormRow(card, gbc, 6, "Stock *", stockField);

        addButton = Theme.createButton("Add", "Add a new product (Alt+A)", KeyEvent.VK_A);
        updateButton = Theme.createButton("Update", "Save changes to the selected product (Alt+U)", KeyEvent.VK_U);
        deleteButton = Theme.createButton("Delete", "Delete the selected product - needs admin password (Alt+D)", KeyEvent.VK_D);
        clearButton = Theme.createButton("Clear", "Clear the form (Alt+C)", KeyEvent.VK_C);
        undoButton = Theme.createButton("Undo Delete", "Restore the last deleted product (Alt+Z)", KeyEvent.VK_Z);
        infoButton = Theme.createButton("View Info", "Show the ingredients, best for and benefits of the selected product (Alt+I)", KeyEvent.VK_I);

        addButton.addActionListener(this);
        updateButton.addActionListener(this);
        deleteButton.addActionListener(this);
        clearButton.addActionListener(this);
        undoButton.addActionListener(this);
        infoButton.addActionListener(this);

        JPanel buttons = new JPanel(new GridLayout(3, 2, 8, 8));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);
        buttons.add(undoButton);
        buttons.add(infoButton);

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = 7;
        gbc.insets = new Insets(12, 2, 3, 2);
        card.add(buttons, gbc);
        gbc.gridy = 8;
        card.add(Theme.createLabel("* required fields   |   ID is auto-numbered"), gbc);
        return card;
    }

    private void addFormRow(JPanel card, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        card.add(Theme.createLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        card.add(field, gbc);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // --- search and sort tools ---
        searchField = Theme.createTextField("Type a name, brand or category");
        findIdField = Theme.createTextField("Type a product ID, example: 1003");
        findIdField.setColumns(6);
        sortBox = new JComboBox<>(SORT_OPTIONS);
        sortBox.setToolTipText("Choose how to sort the table");

        searchButton = Theme.createButton("Search", "Linear search by keyword (Alt+S)", KeyEvent.VK_S);
        findButton = Theme.createButton("Find ID", "Binary search by product ID (Alt+F)", KeyEvent.VK_F);
        sortButton = Theme.createButton("Sort", "Sort the table (Alt+O)", KeyEvent.VK_O);
        showAllButton = Theme.createButton("Show All", "Show all products (Alt+W)", KeyEvent.VK_W);

        searchButton.addActionListener(this);
        searchField.addActionListener(this);    // pressing Enter in the search box also searches
        findButton.addActionListener(this);
        findIdField.addActionListener(this);
        sortButton.addActionListener(this);
        showAllButton.addActionListener(this);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchRow.setOpaque(false);
        searchRow.add(Theme.createLabel("Search:"));
        searchRow.add(searchField);
        searchRow.add(searchButton);
        searchRow.add(Theme.createLabel("   Product ID:"));
        searchRow.add(findIdField);
        searchRow.add(findButton);

        JPanel sortRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        sortRow.setOpaque(false);
        sortRow.add(Theme.createLabel("Sort by:"));
        sortRow.add(sortBox);
        sortRow.add(sortButton);
        sortRow.add(showAllButton);
        // staff do not see the form, so their View Info button is placed here instead
        if (!currentUser.canManageProducts()) {
            sortRow.add(infoButton);
        }

        JPanel tools = new JPanel(new GridLayout(2, 1, 0, 8));
        tools.setOpaque(false);
        tools.add(searchRow);
        tools.add(sortRow);

        // --- product table ---
        tableModel = new DefaultTableModel(COLUMNS, 0);
        table = new JTable(tableModel);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        table.getSelectionModel().addListSelectionListener(this);

        statusLabel = Theme.createLabel("");

        panel.add(tools, BorderLayout.NORTH);
        panel.add(Theme.createScrollPane(table), BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    // ===================== EVENTS =====================

    // Runs when any button is clicked (or Enter is pressed in a search box)
    // [POLYMORPHISM - OVERRIDING] we write our own actionPerformed() for the ActionListener interface
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == addButton) {
            addProduct();
        } else if (source == updateButton) {
            updateProduct();
        } else if (source == deleteButton) {
            deleteProduct();
        } else if (source == clearButton) {
            clearForm();
        } else if (source == undoButton) {
            undoDelete();
        } else if (source == infoButton) {
            showProductInfo();
        } else if (source == searchButton || source == searchField) {
            searchProducts();
        } else if (source == findButton || source == findIdField) {
            findById();
        } else if (source == sortButton) {
            sortProducts();
        } else if (source == showAllButton) {
            searchField.setText("");
            findIdField.setText("");
            refreshTable();
        }
    }

    // Runs when a row of the table is selected: copy that product into the form
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            fillFormFromTable();
        }
    }

    // ===================== TABLE =====================

    public void refreshTable() {
        showProducts(store.getProducts());
    }

    private void showProducts(ArrayList<Product> list) {   // also counts the low stock items
        currentView = new ArrayList<>(list);   // copy, so sorting never changes the real list
        tableModel.setRowCount(0);
        int lowStock = 0;
        for (int i = 0; i < currentView.size(); i++) {
            Product p = currentView.get(i);
            // [POLYMORPHISM] getCategory() and getBestFor() run the version of each product's own subclass
            Object[] row = {p.getId(), p.getCategory(), p.getName(), p.getBrand(),
                Theme.money(p.getPrice()), p.getStock(), p.getBestFor()};
            tableModel.addRow(row);
            if (p.isLowStock()) {
                lowStock++;
            }
        }
        statusLabel.setText("Showing " + currentView.size() + " of " + store.getProducts().size()
                + " products     |     Low stock (" + Product.LOW_STOCK_LEVEL + " or less): " + lowStock);
    }

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int id = (Integer) tableModel.getValueAt(row, 0);
        return store.findProductById(id);
    }

    private void fillFormFromTable() {
        Product p = getSelectedProduct();
        if (p == null) {
            return;
        }
        idField.setText(String.valueOf(p.getId()));
        categoryBox.setSelectedItem(p.getCategory());
        nameField.setText(p.getName());
        brandField.setText(p.getBrand());
        priceField.setText(String.valueOf(p.getPrice()));
        stockField.setText(String.valueOf(p.getStock()));
        resetFieldBorders();
    }

    // ===================== FORM =====================

    private void clearForm() {   // also shows the next free ID
        table.clearSelection();
        idField.setText(String.valueOf(store.getNextProductId()));
        categoryBox.setSelectedIndex(0);
        nameField.setText("");
        brandField.setText("");
        priceField.setText("");
        stockField.setText("");
        resetFieldBorders();
        nameField.requestFocus();
    }

    private void resetFieldBorders() {
        Theme.markField(nameField, false);
        Theme.markField(brandField, false);
        Theme.markField(priceField, false);
        Theme.markField(stockField, false);
    }

    // Reads and checks the whole form.
    // If something is wrong, it shows an error message and returns null.
    private Product readForm(int id) {
        String name = nameField.getText().trim();
        String brand = brandField.getText().trim();
        String priceText = priceField.getText().trim();
        String stockText = stockField.getText().trim();

        Theme.markField(nameField, name.isEmpty());
        Theme.markField(brandField, brand.isEmpty());
        Theme.markField(priceField, priceText.isEmpty());
        Theme.markField(stockField, stockText.isEmpty());
        if (name.isEmpty() || brand.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
            Dialogs.showError(this, "Please fill in all required fields marked with *.");
            return null;
        }
        if (name.contains("|") || brand.contains("|")) {
            Dialogs.showError(this, "Name and brand must not contain the | character.");
            return null;
        }

        // EXCEPTION HANDLING - parseDouble / parseInt fail if the text is not a number
        double price;
        int stock;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            Theme.markField(priceField, true);
            Dialogs.showError(this, "Price must be a number (example: 499.75).");
            return null;
        }
        try {
            stock = Integer.parseInt(stockText);
        } catch (NumberFormatException e) {
            Theme.markField(stockField, true);
            Dialogs.showError(this, "Stock must be a whole number (example: 20).");
            return null;
        }
        if (price <= 0) {
            Theme.markField(priceField, true);
            Dialogs.showError(this, "Price must be greater than zero.");
            return null;
        }
        if (stock < 0) {
            Theme.markField(stockField, true);
            Dialogs.showError(this, "Stock cannot be negative.");
            return null;
        }
        String category = (String) categoryBox.getSelectedItem();
        return Product.create(category, id, name, brand, price, stock);
    }

    // ===================== ACTIONS =====================

    private void addProduct() {
        Product product = readForm(store.getNextProductId());
        if (product == null) {
            return;     // the form had an error
        }
        String error = store.addProduct(product);
        if (!error.isEmpty()) {
            Dialogs.showError(this, error);
            return;
        }
        refreshTable();
        clearForm();
        Dialogs.showInfo(this, "Product #" + product.getId() + " \"" + product.getName() + "\" was added.");
    }

    private void updateProduct() {   // a new category needs a new subclass object, so it gets replaced
        Product selected = getSelectedProduct();
        if (selected == null) {
            Dialogs.showWarning(this, "Please select a product from the table first.");
            return;
        }
        Product edited = readForm(selected.getId());
        if (edited == null) {
            return;     // the form had an error
        }
        if (edited.getCategory().equals(selected.getCategory())) {
            // same category: update the existing object using its setters
            selected.setName(edited.getName());
            selected.setBrand(edited.getBrand());
            selected.setPrice(edited.getPrice());
            selected.setStock(edited.getStock());
            store.saveProducts();
        } else {
            // different category: a different subclass object is needed
            store.replaceProduct(selected, edited);
        }
        refreshTable();
        clearForm();
        Dialogs.showInfo(this, "Product #" + edited.getId() + " was updated.");
    }

    private void deleteProduct() {   // needs a confirm and the admin password
        Product selected = getSelectedProduct();
        if (selected == null) {
            Dialogs.showWarning(this, "Please select a product from the table first.");
            return;
        }
        if (!Dialogs.confirm(this, "Delete product #" + selected.getId() + " \"" + selected.getName() + "\"?")) {
            return;
        }
        if (!Dialogs.askAdminPassword(this, store)) {
            return;
        }
        store.deleteProduct(selected);
        refreshTable();
        clearForm();
        Dialogs.showInfo(this, "Product deleted. You can restore it with \"Undo Delete\".");
    }

    private void undoDelete() {
        Product restored = store.undoDelete();
        if (restored == null) {
            Dialogs.showWarning(this, "There is no deleted product to restore.");
            return;
        }
        refreshTable();
        clearForm();        // the next free ID may have changed
        Dialogs.showInfo(this, "Product #" + restored.getId() + " \"" + restored.getName() + "\" was restored.");
    }

    // Shows the full information of the selected product in a message box.
    // [POLYMORPHISM] getIngredients(), getBestFor() and getBenefits() run the version
    // of the product's own subclass (Serum, Toner, Mask, ...).
    private void showProductInfo() {
        Product p = getSelectedProduct();
        if (p == null) {
            Dialogs.showWarning(this, "Please select a product from the table first.");
            return;
        }
        String info = p.getName() + "\n"
                + "by " + p.getBrand() + "\n\n"
                + "Product ID:  " + p.getId() + "\n"
                + "Category:  " + p.getCategory() + "\n"
                + "Price:  " + Theme.money(p.getPrice()) + "\n"
                + "Stock:  " + p.getStock() + "\n\n"
                + "Key Ingredients:\n" + p.getIngredients() + "\n\n"
                + "Best For:\n" + p.getBestFor() + "\n\n"
                + "Benefits:\n" + p.getBenefits();

        JTextArea area = new JTextArea(info);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Theme.NORMAL_FONT);
        area.setForeground(Theme.TEXT_DARK);
        area.setBackground(Theme.WHITE);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));

        // a scroll pane with a fixed size, so the box never grows and the OK button is always visible
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(480, 400));
        scrollPane.setBorder(null);
        JOptionPane.showMessageDialog(this, scrollPane, "Product Information", JOptionPane.PLAIN_MESSAGE);
    }

    private void searchProducts() {   // linear search
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            Dialogs.showWarning(this, "Please type a keyword to search.");
            return;
        }
        ArrayList<Product> results = SortSearch.linearSearch(store.getProducts(), keyword);
        showProducts(results);
        if (results.isEmpty()) {
            Dialogs.showInfo(this, "No products found for \"" + keyword + "\".");
        }
    }

    private void findById() {   // binary search
        // EXCEPTION HANDLING - the ID must be a number
        try {
            int id = Integer.parseInt(findIdField.getText().trim());
            Product product = store.findProductById(id);
            if (product == null) {
                Dialogs.showInfo(this, "No product found with ID " + id + ".");
                return;
            }
            ArrayList<Product> result = new ArrayList<>();
            result.add(product);
            showProducts(result);
            table.setRowSelectionInterval(0, 0);
        } catch (NumberFormatException e) {
            Dialogs.showError(this, "Please enter a valid numeric product ID (example: 1003).");
        }
    }

    private void sortProducts() {   // depends on what's picked in the drop-down
        ArrayList<Product> sorted = new ArrayList<>(currentView);
        int choice = sortBox.getSelectedIndex();
        if (choice == 1) {
            SortSearch.bubbleSortByPrice(sorted);
        } else if (choice == 2) {
            SortSearch.selectionSortByName(sorted);
        } else {
            SortSearch.insertionSortById(sorted);
        }
        showProducts(sorted);
    }
}
