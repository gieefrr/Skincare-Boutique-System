package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import data.DataStore;
import model.Order;
import model.OrderItem;
import model.Product;
import model.User;

/**
 * ORDERS & SALES TAB
 * - The staff adds one or more products to the ORDER (an ArrayList of OrderItems).
 * - Placing the order puts it in a QUEUE (first come, first served).
 * - Processing an order (after a confirmation) removes it from the front of the queue and prints a receipt.
 * - A wrong order can be cancelled from anywhere in the queue.
 * - Completed sales are pushed to a STACK, so the newest sale is shown first.
 * - Admin only: "Void Last Sale" pops the newest sale and returns its stock.
 *
 * ActionListener: actionPerformed() runs when a button or drop-down list is used.
 */
// [INHERITANCE] OrderPanel extends JPanel
// [ABSTRACTION] implements the ActionListener interface
public class OrderPanel extends JPanel implements ActionListener {

    // ARRAYS: discount names and their matching rates (same index)
    private static final String[] DISCOUNT_NAMES = {"None (0%)", "Loyalty Card (5%)", "Student (10%)", "Senior Citizen / PWD (20%)"};
    private static final double[] DISCOUNT_RATES = {0, 5, 10, 20};

    private DataStore store;
    private User currentUser;

    // the products of the order that is being made
    private ArrayList<OrderItem> orderItems = new ArrayList<>();

    private JTextField customerField;
    private JComboBox<Product> productBox;
    private JSpinner quantitySpinner;
    private JComboBox<String> discountBox;
    private JLabel totalLabel;

    private DefaultTableModel itemModel;
    private JTable itemTable;

    private JButton addItemButton;
    private JButton removeItemButton;
    private JButton placeOrderButton;
    private JButton processButton;
    private JButton cancelButton;
    private JButton voidButton;
    private JButton reportButton;

    private DefaultTableModel queueModel;
    private JTable queueTable;
    private DefaultTableModel historyModel;
    private JLabel summaryLabel;

    public OrderPanel(DataStore store, User currentUser) {   // made in MainFrame.createTabs()
        this.store = store;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Theme.BACKGROUND);

        JPanel lists = new JPanel(new GridLayout(2, 1, 0, 15));
        lists.setOpaque(false);
        lists.add(createQueuePanel());
        lists.add(createHistoryPanel());

        add(Theme.createHeading("Orders & Sales"), BorderLayout.NORTH);
        add(createOrderForm(), BorderLayout.WEST);
        add(lists, BorderLayout.CENTER);

        refreshAll();
    }

    // ===================== LAYOUT =====================

    private JPanel createOrderForm() {
        JPanel card = Theme.createCard();
        card.setLayout(new GridBagLayout());

        customerField = Theme.createTextField("Name of the customer");
        productBox = new JComboBox<>();
        productBox.setToolTipText("Choose a product to add to the order");
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        quantitySpinner.setToolTipText("How many of this product (1 - 100)");
        discountBox = new JComboBox<>(DISCOUNT_NAMES);
        discountBox.setToolTipText("Choose a discount if the customer has one");

        // the order table: one row for each product in the order
        String[] columns = {"Product", "Qty", "Subtotal"};
        itemModel = new DefaultTableModel(columns, 0);
        itemTable = new JTable(itemModel);
        Theme.styleTable(itemTable);
        itemTable.getColumnModel().getColumn(0).setPreferredWidth(190);
        JScrollPane itemScroll = Theme.createScrollPane(itemTable);
        itemScroll.setPreferredSize(new Dimension(320, 130));
        itemScroll.setMinimumSize(new Dimension(320, 100));     // never squeeze the order table too small

        totalLabel = new JLabel(Theme.money(0));
        totalLabel.setFont(Theme.TITLE_FONT);
        totalLabel.setForeground(Theme.ROSE);

        // the total updates right away when the discount changes
        discountBox.addActionListener(this);

        addItemButton = Theme.createButton("Add to Order", "Add the chosen product and quantity to this order (Alt+T)", KeyEvent.VK_T);
        removeItemButton = Theme.createButton("Remove Item", "Remove the selected product from the order (Alt+R)", KeyEvent.VK_R);
        placeOrderButton = Theme.createButton("Place Order", "Put this order with all its products in line (Alt+O)", KeyEvent.VK_O);
        addItemButton.addActionListener(this);
        removeItemButton.addActionListener(this);
        placeOrderButton.addActionListener(this);

        // quantity box with the Add to Order button beside it
        JPanel quantityRow = new JPanel(new BorderLayout(8, 0));
        quantityRow.setOpaque(false);
        quantityRow.add(quantitySpinner, BorderLayout.CENTER);
        quantityRow.add(addItemButton, BorderLayout.EAST);

        // "Order" title with the Remove Item button beside it
        JPanel itemTitleRow = new JPanel(new BorderLayout(8, 0));
        itemTitleRow.setOpaque(false);
        itemTitleRow.add(Theme.createLabel("Order"), BorderLayout.WEST);
        itemTitleRow.add(removeItemButton, BorderLayout.EAST);

        // 2 columns: the label on the left (gridx 0), the input on the right (gridx 1)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 3, 4, 3);

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        card.add(Theme.createHeading("New Order"), gbc);
        gbc.gridwidth = 1;

        addRow(card, gbc, 1, "Customer *", customerField);
        addRow(card, gbc, 2, "Product *", productBox);
        addRow(card, gbc, 3, "Quantity *", quantityRow);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 3, 4, 3);
        card.add(itemTitleRow, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(4, 3, 4, 3);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;                // the order table gets all the extra space
        card.add(itemScroll, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.gridwidth = 1;

        addRow(card, gbc, 6, "Discount", discountBox);

        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridwidth = 1;

        addRow(card, gbc, 8, "Amount to Pay:", totalLabel);

        gbc.gridy = 9;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        card.add(placeOrderButton, gbc);
        return card;
    }

    // Puts a label in the left column and its input in the right column of the same row
    private void addRow(JPanel card, GridBagConstraints gbc, int row, String label, JComponent input) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        card.add(Theme.createLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        card.add(input, gbc);
    }

    private JPanel createQueuePanel() {
        String[] columns = {"Order #", "Customer", "Products", "Items", "Total"};
        queueModel = new DefaultTableModel(columns, 0);
        queueTable = new JTable(queueModel);
        Theme.styleTable(queueTable);
        queueTable.getColumnModel().getColumn(2).setPreferredWidth(320);

        processButton = Theme.createButton("Process Next Order", "Serve the first order in line and print a receipt (Alt+P)", KeyEvent.VK_P);
        cancelButton = Theme.createButton("Cancel Selected Order", "Remove the order selected in the table from the line (Alt+N)", KeyEvent.VK_N);
        processButton.addActionListener(this);
        cancelButton.addActionListener(this);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        buttons.add(processButton);
        buttons.add(cancelButton);

        JPanel card = Theme.createCard();
        card.setLayout(new BorderLayout(8, 8));
        card.add(Theme.createHeading("Order Queue"), BorderLayout.NORTH);
        card.add(Theme.createScrollPane(queueTable), BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createHistoryPanel() {
        String[] columns = {"Order #", "Date", "Customer", "Products", "Items", "Total"};
        historyModel = new DefaultTableModel(columns, 0);
        JTable historyTable = new JTable(historyModel);
        Theme.styleTable(historyTable);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(320);

        summaryLabel = Theme.createLabel("");
        summaryLabel.setFont(Theme.BUTTON_FONT);

        reportButton = Theme.createButton("Sales Report", "Show the weekly or monthly sales report (Alt+E)", KeyEvent.VK_E);
        reportButton.addActionListener(this);
        voidButton = Theme.createButton("Void Last Sale", "Cancel the newest sale and return its stock - admin only (Alt+V)", KeyEvent.VK_V);
        voidButton.addActionListener(this);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(reportButton);
        // [POLYMORPHISM] canManageProducts() runs the Admin or Staff version:
        // only the admin (manager) gets the Void button
        if (currentUser.canManageProducts()) {
            buttons.add(voidButton);
        }

        // the summary on top, the buttons under it
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(summaryLabel, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.SOUTH);

        JPanel card = Theme.createCard();
        card.setLayout(new BorderLayout(8, 8));
        card.add(Theme.createHeading("Sales History"), BorderLayout.NORTH);
        card.add(Theme.createScrollPane(historyTable), BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // ===================== EVENTS =====================

    // [POLYMORPHISM - OVERRIDING] we write our own actionPerformed() for the ActionListener interface
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == addItemButton) {
            addToOrder();
        } else if (source == removeItemButton) {
            removeFromOrder();
        } else if (source == placeOrderButton) {
            placeOrder();
        } else if (source == processButton) {
            processNextOrder();
        } else if (source == cancelButton) {
            cancelSelectedOrder();
        } else if (source == voidButton) {
            voidLastSale();
        } else if (source == reportButton) {
            generateReport();
        } else if (source == discountBox) {
            updateTotal();
        }
    }

    // ===================== REFRESH =====================

    public void refreshAll() {
        refreshProductBox();
        refreshOrderItems();
        refreshQueue();
        refreshHistory();
    }

    private void refreshProductBox() {
        Product selected = (Product) productBox.getSelectedItem();
        productBox.removeAllItems();
        ArrayList<Product> products = store.getProducts();
        for (int i = 0; i < products.size(); i++) {
            productBox.addItem(products.get(i));
        }
        if (selected != null && products.contains(selected)) {
            productBox.setSelectedItem(selected);
        }
    }

    private void refreshOrderItems() {
        itemModel.setRowCount(0);
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem item = orderItems.get(i);
            Object[] row = {item.getProductName(), item.getQuantity(), Theme.money(item.getSubtotal())};
            itemModel.addRow(row);
        }
        updateTotal();
    }

    private void refreshQueue() {
        queueModel.setRowCount(0);
        // copy the queue into an ArrayList so we can loop with an index (front of the line first)
        ArrayList<Order> waiting = new ArrayList<>(store.getOrderQueue());
        for (int i = 0; i < waiting.size(); i++) {
            Order order = waiting.get(i);
            Object[] row = {order.getOrderId(), order.getCustomerName(), order.getProductNames(),
                order.getTotalQuantity(), Theme.money(order.getTotal())};
            queueModel.addRow(row);
        }
    }

    private void refreshHistory() {   // newest sale on top
        historyModel.setRowCount(0);
        Stack<Order> history = store.getSalesHistory();
        for (int i = history.size() - 1; i >= 0; i--) {     // top of the stack (newest) first
            Order order = history.get(i);
            Object[] row = {order.getOrderId(), order.getDate(), order.getCustomerName(), order.getProductNames(),
                order.getTotalQuantity(), Theme.money(order.getTotal())};
            historyModel.addRow(row);
        }
        summaryLabel.setText("Completed: " + history.size()
                + "   |   Total sales: " + Theme.money(store.getTotalSales())
                + "   |   In queue: " + store.getOrderQueue().size());
    }

    // Amount to pay = every item in the order with the discount applied
    private void updateTotal() {
        double discount = DISCOUNT_RATES[discountBox.getSelectedIndex()];
        double total = 0;
        for (int i = 0; i < orderItems.size(); i++) {
            Product product = store.findProductById(orderItems.get(i).getProductId());
            if (product != null) {
                // [POLYMORPHISM] for a Sunscreen, Java runs Sunscreen's computeTotal() with the promo
                total = total + product.computeTotal(orderItems.get(i).getQuantity(), discount);
            }
        }
        totalLabel.setText(Theme.money(total));
    }

    // Returns the position of a product in the order, or -1 if it is not in the order yet
    private int findInOrder(int productId) {
        for (int i = 0; i < orderItems.size(); i++) {
            if (orderItems.get(i).getProductId() == productId) {
                return i;
            }
        }
        return -1;
    }

    // ===================== ACTIONS =====================

    private void addToOrder() {   // checks stock first, OrderItem can't change so it gets replaced
        Product product = (Product) productBox.getSelectedItem();
        if (product == null) {
            Dialogs.showError(this, "There are no products available. Please add a product first.");
            return;
        }
        int quantity = (Integer) quantitySpinner.getValue();

        // if the product is already in the order, add the new quantity to the old one
        int index = findInOrder(product.getId());
        if (index != -1) {
            quantity = quantity + orderItems.get(index).getQuantity();
        }
        if (quantity > product.getStock()) {
            Dialogs.showError(this, "Not enough stock. Only " + product.getStock() + " left for "
                    + product.getName() + ".");
            return;
        }

        // [POLYMORPHISM] for a Sunscreen, Java runs Sunscreen's computeTotal() with the promo
        OrderItem item = new OrderItem(product.getId(), product.getName(), quantity, product.computeTotal(quantity));
        if (index != -1) {
            orderItems.set(index, item);      // OrderItem cannot be changed, so we replace it with a new one
        } else {
            orderItems.add(item);
        }
        quantitySpinner.setValue(1);
        refreshOrderItems();
    }

    private void removeFromOrder() {
        int row = itemTable.getSelectedRow();
        if (row == -1) {
            Dialogs.showWarning(this, "Please select a product in the order first.");
            return;
        }
        orderItems.remove(row);
        refreshOrderItems();
    }

    private void placeOrder() {   // the order goes to the queue here
        String customer = customerField.getText().trim();
        Theme.markField(customerField, customer.isEmpty());
        if (customer.isEmpty()) {
            Dialogs.showError(this, "Please enter the customer name.");
            return;
        }
        if (customer.contains("|")) {
            Dialogs.showError(this, "Customer name must not contain the | character.");
            return;
        }
        if (orderItems.isEmpty()) {
            Dialogs.showError(this, "The order is empty. Please add at least one product.");
            return;
        }
        double discount = DISCOUNT_RATES[discountBox.getSelectedIndex()];
        Order order = store.placeOrder(customer, orderItems, discount);

        // start a new empty order
        orderItems = new ArrayList<>();
        customerField.setText("");
        quantitySpinner.setValue(1);
        discountBox.setSelectedIndex(0);
        refreshOrderItems();
        refreshQueue();
        refreshHistory();
        Dialogs.showInfo(this, "Order #" + order.getOrderId() + " for " + customer + " ("
                + order.getItemCount() + " product/s) was added to the queue.\n"
                + "Position in line: " + store.getOrderQueue().size());
    }

    private void processNextOrder() {   // confirm first, then show the receipt
        Order next = store.getOrderQueue().peek();
        if (next == null) {
            Dialogs.showWarning(this, "The order queue is empty.");
            return;
        }
        // let the staff check the order first, so a wrong order is not sold by mistake
        if (!Dialogs.confirm(this, "Process this order?\n\n"
                + "Order #" + next.getOrderId() + " - " + next.getCustomerName() + "\n"
                + "Products: " + next.getProductNames() + "\n"
                + "Total: " + Theme.money(next.getTotal()))) {
            return;
        }
        Order order = store.processNextOrder();
        if (order == null) {
            Dialogs.showError(this, "Order #" + next.getOrderId()
                    + " was cancelled because a product is out of stock or was deleted.");
        } else {
            showReceipt(order);
        }
        refreshAll();
    }

    // Cancels the order the staff clicked in the queue table (a wrong order or the customer left)
    private void cancelSelectedOrder() {
        int row = queueTable.getSelectedRow();
        if (row == -1) {
            Dialogs.showWarning(this, "Please select the order to cancel in the Order Queue table first.");
            return;
        }
        // the table rows are in the same order as the queue (front of the line first)
        ArrayList<Order> waiting = new ArrayList<>(store.getOrderQueue());
        Order order = waiting.get(row);
        if (Dialogs.confirm(this, "Cancel order #" + order.getOrderId() + " of " + order.getCustomerName() + "?\n"
                + "Products: " + order.getProductNames())) {
            store.cancelOrder(order);
            refreshAll();
        }
    }

    // ADMIN ONLY: a wrong order was already processed, so the last sale is voided
    // and its stock is returned. The admin password is asked first.
    private void voidLastSale() {
        if (store.getSalesHistory().isEmpty()) {
            Dialogs.showWarning(this, "There are no sales to void.");
            return;
        }
        Order last = store.getSalesHistory().peek();     // PEEK: look at the newest sale without removing it
        if (!Dialogs.confirm(this, "Void the last sale?\n\n"
                + "Order #" + last.getOrderId() + " - " + last.getCustomerName() + "\n"
                + "Products: " + last.getProductNames() + "\n"
                + "Total: " + Theme.money(last.getTotal()) + "\n\n"
                + "The stock of these products will be returned.")) {
            return;
        }
        if (!Dialogs.askAdminPassword(this, store)) {
            return;
        }
        Order voided = store.voidLastSale();
        refreshAll();
        Dialogs.showInfo(this, "Order #" + voided.getOrderId() + " was voided and its stock was returned.");
    }

    // ===================== SALES REPORT =====================

    // Asks for Weekly or Monthly, then finds the start date of that period
    private void generateReport() {
        String[] choices = {"Weekly", "Monthly"};
        int choice = JOptionPane.showOptionDialog(this, "Which sales report do you want to see?", "Sales Report",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
        if (choice == -1) {
            return;     // the box was closed
        }

        Calendar calendar = Calendar.getInstance();     // today
        String title;
        if (choice == 0) {
            calendar.add(Calendar.DAY_OF_MONTH, -6);    // today and the 6 days before it = 7 days
            title = "WEEKLY SALES REPORT";
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, 1);     // the 1st day of this month
            title = "MONTHLY SALES REPORT";
        }
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = dayFormat.format(calendar.getTime());
        String today = dayFormat.format(new Date());

        ArrayList<Order> sales = store.getSalesFrom(startDate);
        showReport(title, startDate, today, sales);
    }

    // Builds the report text: totals, products sold, and the list of orders
    private void showReport(String title, String startDate, String endDate, ArrayList<Order> sales) {
        String line = "--------------------------------------------------\n";
        String report = "                 RADIANT GLOW\n"
                + "            " + title + "\n"
                + "         " + startDate + "  to  " + endDate + "\n"
                + line;

        if (sales.isEmpty()) {
            report = report + "No completed sales in this period.\n";
        } else {
            // add up the totals, and count each product sold (parallel ArrayLists, same index)
            double totalSales = 0;
            int totalItems = 0;
            ArrayList<String> productNames = new ArrayList<>();
            ArrayList<Integer> productQuantities = new ArrayList<>();
            for (int i = 0; i < sales.size(); i++) {
                Order order = sales.get(i);
                totalSales = totalSales + order.getTotal();
                ArrayList<OrderItem> items = order.getItems();
                for (int j = 0; j < items.size(); j++) {
                    OrderItem item = items.get(j);
                    totalItems = totalItems + item.getQuantity();
                    int index = productNames.indexOf(item.getProductName());     // linear search
                    if (index == -1) {
                        productNames.add(item.getProductName());
                        productQuantities.add(item.getQuantity());
                    } else {
                        productQuantities.set(index, productQuantities.get(index) + item.getQuantity());
                    }
                }
            }

            // the best seller is the product with the biggest quantity
            int best = 0;
            for (int i = 1; i < productQuantities.size(); i++) {
                if (productQuantities.get(i) > productQuantities.get(best)) {
                    best = i;
                }
            }

            report = report
                    + "Completed orders : " + sales.size() + "\n"
                    + "Items sold       : " + totalItems + "\n"
                    + "Total sales      : " + Theme.money(totalSales) + "\n"
                    + "Average per order: " + Theme.money(totalSales / sales.size()) + "\n"
                    + "Best seller      : " + productNames.get(best) + " (" + productQuantities.get(best) + " pcs)\n"
                    + line
                    + "PRODUCTS SOLD\n";
            for (int i = 0; i < productNames.size(); i++) {
                report = report + "  " + productQuantities.get(i) + " x " + productNames.get(i) + "\n";
            }
            report = report + line + "ORDERS\n";
            for (int i = 0; i < sales.size(); i++) {
                Order order = sales.get(i);
                report = report + "  #" + order.getOrderId() + "  " + order.getDate() + "  "
                        + order.getCustomerName() + "  " + Theme.money(order.getTotal()) + "\n";
            }
        }

        JTextArea area = new JTextArea(report);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        // fixed size with a scroll bar, so a long report never pushes the OK button off the screen
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(560, 420));
        JOptionPane.showMessageDialog(this, scrollPane, "Sales Report", JOptionPane.PLAIN_MESSAGE);
    }

    private void showReceipt(Order order) {
        String line = "----------------------------------------\n";
        String receipt = "             RADIANT GLOW\n"
                + "            Official Receipt\n"
                + line
                + "Order #  : " + order.getOrderId() + "\n"
                + "Date     : " + order.getDate() + "\n"
                + "Customer : " + order.getCustomerName() + "\n"
                + line;

        // one line for every product in the order
        ArrayList<OrderItem> items = order.getItems();
        double subtotal = 0;
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            receipt = receipt + item.getQuantity() + " x " + item.getProductName() + "\n"
                    + "           " + Theme.money(item.getSubtotal()) + "\n";
            subtotal = subtotal + item.getSubtotal();
        }

        receipt = receipt + line
                + "Subtotal : " + Theme.money(subtotal) + "\n"
                + "Discount : " + order.getDiscount() + "%\n"
                + "TOTAL    : " + Theme.money(order.getTotal()) + "\n"
                + line
                + "      Thank you! Keep glowing! <3\n";

        JTextArea area = new JTextArea(receipt);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setBackground(Theme.WHITE);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        JOptionPane.showMessageDialog(this, area, "Receipt - Order #" + order.getOrderId(), JOptionPane.PLAIN_MESSAGE);
    }
}
