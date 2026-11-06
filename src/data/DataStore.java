package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import model.Admin;
import model.Cleanser;
import model.Exfoliant;
import model.EyeCream;
import model.Mask;
import model.Moisturizer;
import model.Order;
import model.OrderItem;
import model.Product;
import model.Serum;
import model.SpotTreatment;
import model.Staff;
import model.Sunscreen;
import model.Toner;
import model.Treatment;
import model.User;

/**
 * Holds ALL the data of the system and saves it to text files (our database).
 *
 * DATA STRUCTURES USED:
 *  - ArrayList<User>, ArrayList<Product> : main lists of records
 *  - HashMap<String, User>               : hash table for fast login lookup by username
 *  - Queue<Order>                        : pending customer orders (First In, First Out)
 *  - Stack<Order>                        : sales history (Last In, First Out - newest on top)
 *  - Stack<Product>                      : recently deleted products for "Undo Delete"
 */
public class DataStore {   // no constructor written, new DataStore() in Main uses Java's default one

    private static final String USERS_FILE = "data/users.txt";
    private static final String PRODUCTS_FILE = "data/products.txt";
    private static final String SALES_FILE = "data/sales.txt";
    private static final String SALE_ITEMS_FILE = "data/sale_items.txt";

    // [ENCAPSULATION] the data is private; screens can only use it through DataStore's methods
    // [POLYMORPHISM] the lists hold User and Product, but the real objects are Admin/Staff and Serum/Mask/...
    private ArrayList<User> users = new ArrayList<>();
    private HashMap<String, User> userTable = new HashMap<>();
    private ArrayList<Product> products = new ArrayList<>();     // always kept sorted by ID
    private Queue<Order> orderQueue = new LinkedList<>();
    private Stack<Order> salesHistory = new Stack<>();
    private Stack<Product> deletedProducts = new Stack<>();

    private static final int FIRST_PRODUCT_ID = 1001;   // auto-numbered product IDs start here
    private int nextOrderId = 1;                   // auto-numbered order numbers

    // USERS

    public ArrayList<User> getUsers() {
        return users;
    }

    // Hash table lookup: much faster than checking every user one by one
    public User findUser(String username) {
        return userTable.get(username.toLowerCase());
    }

    // Returns an error message, or an empty String "" if the user was added
    public String addUser(User user) {
        String error = User.validateUsername(user.getUsername());
        if (!error.isEmpty()) {
            return error;
        }
        if (findUser(user.getUsername()) != null) {
            return "Username \"" + user.getUsername() + "\" is already taken.";
        }
        putUser(user);
        saveUsers();
        return "";
    }

    public void deleteUser(User user) {   // has to remove from the list and the hash table
        users.remove(user);
        userTable.remove(user.getUsername().toLowerCase());
        saveUsers();
    }

    public int countAdmins() {   // UserPanel needs this so the last admin can't be deleted
        int count = 0;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getRole().equals("Admin")) {
                count++;
            } 
        }
        return count;
    }
    // Used before deleting
    public boolean isAdminPassword(String password) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getRole().equals("Admin") && !user.isBlocked() && user.checkPassword(password)) {
                return true;
            }
        }
        return false;
    }

    private void putUser(User user) {   // keeps the list and the hash table the same
        users.add(user);
        userTable.put(user.getUsername().toLowerCase(), user);
    }

    // PRODUCTS

    public ArrayList<Product> getProducts() {
        return products;
    }

    // Finds the smallest free ID starting from 1001
    public int getNextProductId() {
        int id = FIRST_PRODUCT_ID;
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == id) {
                id++;
            } else if (products.get(i).getId() > id) {
                break;          // found a gap
            }
        }
        return id;
    }

    // Puts the product in the correct place so the list stays sorted by ID
    private void insertSorted(Product product) {
        int index = 0;
        while (index < products.size() && products.get(index).getId() < product.getId()) {
            index++;
        }
        products.add(index, product);
    }

    // Uses BINARY SEARCH because the product list is always sorted by ID
    public Product findProductById(int id) {
        return SortSearch.binarySearchById(products, id);
    }

    // Returns an error message, or an empty String "" if the product was added
    public String addProduct(Product product) {
        // prevent redundant (duplicate) records
        for (int i = 0; i < products.size(); i++) {
            Product existing = products.get(i);
            if (existing.getName().equalsIgnoreCase(product.getName())
                    && existing.getBrand().equalsIgnoreCase(product.getBrand())) {
                return "\"" + product.getName() + "\" by " + product.getBrand()
                        + " already exists (ID " + existing.getId() + ").";
            }
        }
        insertSorted(product);                  

        // a deleted product with the same ID can no longer be restored (the ID is taken now)
        for (int i = deletedProducts.size() - 1; i >= 0; i--) {
            if (deletedProducts.get(i).getId() == product.getId()) {
                deletedProducts.remove(i);
            }
        }
        saveProducts();
        return "";
    }

    // Used when the category of a product is changed (a different subclass is needed)
    public void replaceProduct(Product oldProduct, Product newProduct) {
        int index = products.indexOf(oldProduct);
        products.set(index, newProduct);
        saveProducts();
    }

    public void deleteProduct(Product product) {   // goes on the stack so Undo Delete can bring it back
        products.remove(product);
        deletedProducts.push(product);          // remember it so it can be restored
        saveProducts();
    }

    // Returns the restored product, or null if nothing was deleted
    public Product undoDelete() {
        if (deletedProducts.isEmpty()) {
            return null;
        }
        Product product = deletedProducts.pop(); // the LAST deleted product comes out first
        insertSorted(product);                   // put it back so the list stays sorted by ID
        saveProducts();
        return product;
    }

    // ===================== ORDERS AND SALES =====================

    public Queue<Order> getOrderQueue() {
        return orderQueue;
    }

    public Stack<Order> getSalesHistory() {
        return salesHistory;
    }

    // ENQUEUE: the new order (with all its items) goes to the back of the line
    // (the order form checks the stock before calling this)
    public Order placeOrder(String customer, ArrayList<OrderItem> items, double discountPercent) {
        double total = 0;
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            Product product = findProductById(item.getProductId());
            if (product != null) {
                // [POLYMORPHISM] for a Sunscreen, Java runs Sunscreen's computeTotal() with the promo
                total = total + product.computeTotal(item.getQuantity(), discountPercent);
            }
        }
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(new Date());
        Order order = new Order(nextOrderId, customer, items, discountPercent, total, date);
        nextOrderId++;
        orderQueue.offer(order);
        return order;
    }

    // DEQUEUE: the order at the front of the line is served first.
    // Returns the finished order, or null if it had to be cancelled
    // (one of its products is out of stock or was deleted).
    // The order panel checks that the queue is not empty before calling this.
    public Order processNextOrder() {
        Order order = orderQueue.poll();
        ArrayList<OrderItem> items = order.getItems();

        // 1st loop: check EVERY item first, so we never sell only half of an order
        for (int i = 0; i < items.size(); i++) {
            Product product = findProductById(items.get(i).getProductId());
            if (product == null || product.getStock() < items.get(i).getQuantity()) {
                return null;
            }
        }
        // 2nd loop: all items are available, so lower the stock of each one
        for (int i = 0; i < items.size(); i++) {
            Product product = findProductById(items.get(i).getProductId());
            product.setStock(product.getStock() - items.get(i).getQuantity());
        }
        salesHistory.push(order);               // PUSH: newest sale is on top of the stack
        saveProducts();
        saveSales();
        return order;
    }

    // Removes a wrong order from the line (for example the customer changed their mind).
    // The other orders keep their places, so the queue is still First In, First Out.
    public void cancelOrder(Order order) {
        orderQueue.remove(order);
    }

    // VOID the last sale (admin only): POP the newest sale from the stack and return its stock.
    // Returns the voided order, or null if there are no sales yet.
    public Order voidLastSale() {
        if (salesHistory.isEmpty()) {
            return null;
        }
        Order order = salesHistory.pop();       // POP: the LAST sale comes out first
        ArrayList<OrderItem> items = order.getItems();
        for (int i = 0; i < items.size(); i++) {
            Product product = findProductById(items.get(i).getProductId());
            if (product != null) {
                product.setStock(product.getStock() + items.get(i).getQuantity());   // put the items back
            }
        }
        saveProducts();
        saveSales();
        return order;
    }

    // For the weekly and monthly reports: the completed sales from startDate (example "2026-10-22") until today.
    // Dates are saved as "yyyy-MM-dd ...", so comparing the text also compares the dates.
    public ArrayList<Order> getSalesFrom(String startDate) {
        ArrayList<Order> result = new ArrayList<>();
        for (int i = 0; i < salesHistory.size(); i++) {
            Order order = salesHistory.get(i);
            String orderDay = order.getDate().substring(0, 10);     // "2026-10-28 02:15 PM" -> "2026-10-28"
            if (orderDay.compareTo(startDate) >= 0) {
                result.add(order);
            }
        }
        return result;
    }

    public double getTotalSales() {
        double total = 0;
        for (int i = 0; i < salesHistory.size(); i++) {
            total = total + salesHistory.get(i).getTotal();
        }
        return total;
    }

    // ===================== FILE HANDLING =====================

    public void loadAll() {   // called once from Main
        new File("data").mkdirs();      // creates the data folder if it does not exist yet
        loadUsers();
        loadProducts();
        loadSales();
    }

    private void loadUsers() {   // status comes from the file so blocked users stay blocked
        ArrayList<String> lines = readLines(USERS_FILE);
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\\|");
            if (parts.length != 4) {
                continue;                       // skip broken lines
            }
            if (parts[0].equals("Admin")) {
                putUser(new Admin(parts[1], parts[2], parts[3]));
            } else {
                putUser(new Staff(parts[1], parts[2], parts[3]));
            }
        }
        // first run: create default accounts so the system can be used
        if (users.isEmpty()) {
            putUser(new Admin("admin", "admin123"));
            putUser(new Staff("staff", "staff123"));
            saveUsers();
        }
    }

    private void loadProducts() {   // Product.create() makes the right subclass from the category
        boolean firstRun = !new File(PRODUCTS_FILE).exists();
        ArrayList<String> lines = readLines(PRODUCTS_FILE);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split("\\|");
            if (parts.length != 6) {
                continue;
            }
            // EXCEPTION HANDLING - parseInt / parseDouble fail if the text is not a number
            try {
                products.add(Product.create(parts[0], Integer.parseInt(parts[1]), parts[2], parts[3],
                        Double.parseDouble(parts[4]), Integer.parseInt(parts[5])));
            } catch (NumberFormatException e) {
                System.out.println("Skipped invalid product line: " + line);
            }
        }
        if (firstRun) {
            addSampleProducts();
            saveProducts();
        }
        SortSearch.insertionSortById(products);   // binary search needs a sorted list
    }

    // Reads sales.txt (one line per order) and sale_items.txt (one line per item of an order)
    private void loadSales() {
        ArrayList<String> itemLines = readLines(SALE_ITEMS_FILE);
        ArrayList<String> lines = readLines(SALES_FILE);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split("\\|");
            if (parts.length != 5) {
                continue;
            }
            try {
                int orderId = Integer.parseInt(parts[0]);
                ArrayList<OrderItem> items = loadItemsOfOrder(itemLines, orderId);
                Order order = new Order(orderId, parts[1], items, Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3]), parts[4]);
                salesHistory.push(order);
                if (order.getOrderId() >= nextOrderId) {
                    nextOrderId = order.getOrderId() + 1;
                }
            } catch (NumberFormatException e) {
                System.out.println("Skipped invalid sales line: " + line);
            }
        }
    }

    // Finds the lines in sale_items.txt that belong to one order (linear search by order number)
    private ArrayList<OrderItem> loadItemsOfOrder(ArrayList<String> itemLines, int orderId) {
        ArrayList<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < itemLines.size(); i++) {
            String[] parts = itemLines.get(i).split("\\|");
            if (parts.length != 5) {
                continue;
            }
            try {
                if (Integer.parseInt(parts[0]) == orderId) {
                    items.add(new OrderItem(Integer.parseInt(parts[1]), parts[2],
                            Integer.parseInt(parts[3]), Double.parseDouble(parts[4])));
                }
            } catch (NumberFormatException e) {
                System.out.println("Skipped invalid item line: " + itemLines.get(i));
            }
        }
        return items;
    }

    private void addSampleProducts() {   // 15 sample products to start with
        products.add(new Cleanser(1001, "Gentle Foaming Cleanser", "CeraVe", 650, 25));
        products.add(new Cleanser(1002, "Rice Water Cleanser", "The Face Shop", 399, 4));
        products.add(new Serum(1003, "Niacinamide 10% Serum", "The Ordinary", 590, 18));
        products.add(new Serum(1004, "Vitamin C Brightening Serum", "Garnier", 499, 12));
        products.add(new Moisturizer(1005, "Hydro Boost Water Gel", "Neutrogena", 899, 10));
        products.add(new Moisturizer(1006, "Snail Mucin Cream", "COSRX", 1150, 3));
        products.add(new Sunscreen(1007, "UV Aqua Rich SPF50", "Biore", 749, 20));
        products.add(new Sunscreen(1008, "Sun Serum SPF50+", "Skin1004", 825, 8));
        products.add(new Toner(1009, "Supple Preparation Facial Toner", "Klairs", 850, 14));
        products.add(new Exfoliant(1010, "AHA BHA PHA Miracle Toner", "Some By Mi", 780, 9));
        products.add(new Exfoliant(1011, "Glycolic Acid 7% Toning Solution", "The Ordinary", 620, 15));
        products.add(new Treatment(1012, "Azelaic Acid Suspension 10%", "The Ordinary", 560, 11));
        products.add(new Mask(1013, "Super Volcanic Pore Clay Mask", "Innisfree", 795, 7));
        products.add(new EyeCream(1014, "Eye Repair Cream", "CeraVe", 890, 6));
        products.add(new SpotTreatment(1015, "Acne Pimple Master Patch", "COSRX", 299, 30));
    }

    public void saveUsers() {
        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            lines.add(users.get(i).toFileString());
        }
        writeLines(USERS_FILE, lines);
    }

    public void saveProducts() {
        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            lines.add(products.get(i).toFileString());
        }
        writeLines(PRODUCTS_FILE, lines);
    }

    public void saveSales() {   // two files: sales.txt for orders, sale_items.txt for their items
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> itemLines = new ArrayList<>();
        for (int i = 0; i < salesHistory.size(); i++) {      // bottom (oldest) to top (newest)
            Order order = salesHistory.get(i);
            lines.add(order.toFileString());
            ArrayList<OrderItem> items = order.getItems();
            for (int j = 0; j < items.size(); j++) {
                itemLines.add(items.get(j).toFileString(order.getOrderId()));
            }
        }
        writeLines(SALES_FILE, lines);
        writeLines(SALE_ITEMS_FILE, itemLines);
    }

    // Reads every non-empty line of a text file into an ArrayList
    private ArrayList<String> readLines(String fileName) {
        ArrayList<String> lines = new ArrayList<>();
        File file = new File(fileName);
        if (!file.exists()) {
            return lines;
        }
        // EXCEPTION HANDLING - reading a file can fail (IOException)
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Could not read " + fileName + ": " + e.getMessage());
        }
        return lines;
    }

    // Writes each item of the ArrayList as one line of a text file
    private void writeLines(String fileName, ArrayList<String> lines) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(fileName));
            for (int i = 0; i < lines.size(); i++) {
                writer.println(lines.get(i));
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Could not save " + fileName + ": " + e.getMessage());
        }
    }
}
