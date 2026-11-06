package model;

/**
 * IMMUTABLE CLASS - one line of an order: which product, how many, and its subtotal.
 * An Order can hold many OrderItems (one for each product).
 */
public final class OrderItem {

    // [ENCAPSULATION] private final fields with getters only (no setters, so they can never change)
    private final int productId;
    private final String productName;
    private final int quantity;
    private final double subtotal;      // price x quantity (with the sunscreen promo), before the discount

    public OrderItem(int productId, String productName, int quantity, double subtotal) {   // made in addToOrder() and when loading saved sales
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }

    // One line in sale_items.txt, example: 1|1001|Gentle Foaming Cleanser|2|1300.0
    public String toFileString(int orderId) {
        return orderId + "|" + productId + "|" + productName + "|" + quantity + "|" + subtotal;
    }
}
