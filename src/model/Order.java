package model;

import java.util.ArrayList;

/**
 * IMMUTABLE CLASS - once an order is created it can never be changed.
 * Rules followed: the class is final, all fields are private final, and there are no setters.
 * One order can hold MANY products. Each product line is an OrderItem.
 */
public final class Order {

    // [ENCAPSULATION] private fields with getters only (no setters, so they can never change)
    private final int orderId;
    private final String customerName;
    private final ArrayList<OrderItem> items;
    private final double discount;      // discount in percent, example: 20.0
    private final double total;
    private final String date;

    public Order(int orderId, String customerName, ArrayList<OrderItem> items,   // made in placeOrder() and loadSales() in DataStore
                 double discount, double total, String date) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.items = new ArrayList<>(items);    // keep our own copy so nobody outside can change it
        this.discount = discount;
        this.total = total;
        this.date = date;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    // [ENCAPSULATION] returns a copy, so the list inside the order cannot be changed
    public ArrayList<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public int getItemCount() {
        return items.size();
    }

    public double getDiscount() {
        return discount;
    }

    public double getTotal() {
        return total;
    }

    public String getDate() {
        return date;
    }

    // Adds up the quantities of all items, example: 2 cleansers + 1 serum = 3
    public int getTotalQuantity() {
        int count = 0;
        for (int i = 0; i < items.size(); i++) {
            count = count + items.get(i).getQuantity();
        }
        return count;
    }

    // Product names joined by commas, used in the tables
    public String getProductNames() {
        String names = "";
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                names = names + ", ";
            }
            names = names + items.get(i).getProductName() + " x" + items.get(i).getQuantity();
        }
        return names;
    }

    // One line in sales.txt (the items are saved in sale_items.txt)
    public String toFileString() {
        return orderId + "|" + customerName + "|" + discount + "|" + total + "|" + date;
    }
}
