package model;

/**
 * [ABSTRACTION] Product is an ABSTRACT CLASS for every skincare product,
 * and it implements the Sellable interface.
 * [INHERITANCE] Subclasses (one per category): Cleanser, Toner, Serum, Moisturizer,
 * Sunscreen, Exfoliant, Treatment, Mask, EyeCream, SpotTreatment.
 */
public abstract class Product implements Sellable {   // parent of the 10 product types

    public static final String[] CATEGORIES = {"Cleanser", "Toner", "Serum", "Moisturizer", "Sunscreen",
        "Exfoliant", "Treatment", "Mask", "Eye Cream", "Spot Treatment"};
    public static final int LOW_STOCK_LEVEL = 5;

    // [ENCAPSULATION] private fields: only reached through getters and setters
    private int id;
    private String name;
    private String brand;
    private double price;
    private int stock;

    // [POLYMORPHISM - OVERLOADING] two constructors with different parameters
    public Product(int id, String name, String brand, double price, int stock) {   // the subclasses call this using super()
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.stock = stock;
    }

    public Product(int id, String name, double price) {   // no subclass uses this one yet, it just shows overloading
        this(id, name, "No Brand", price, 0);
    }

    // [ABSTRACTION] abstract methods: each product type must give its own answer
    public abstract String getCategory();

    public abstract String getIngredients();   // key ingredients of this type of product

    public abstract String getBestFor();       // skin types or concerns it is best for

    public abstract String getBenefits();      // what it does for the skin
    
    //---
    // [ENCAPSULATION] getters
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getBrand() {
        return brand;
    }
    public double getPrice() {
        return price;
    }
    public int getStock() {
        return stock;
    }
    //---
    
    // [ENCAPSULATION] setters (the product form checks the values before calling these)
    public void setName(String name) {
        this.name = name;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }
    public boolean isLowStock() {   // 5 or less counts as low
        return stock <= LOW_STOCK_LEVEL;
    }

    // [ABSTRACTION] this method comes from the Sellable interface: price x quantity
    @Override
    public double computeTotal(int quantity) {
        return price * quantity;
    }

    // [POLYMORPHISM - OVERLOADING] same name, but with a discount (example: 20 for 20% off)
    @Override
    public double computeTotal(int quantity, double discountPercent) {
        // [POLYMORPHISM - DYNAMIC BINDING] if this product is a Sunscreen,
        // Java runs Sunscreen's computeTotal(), so the promo is applied
        double total = computeTotal(quantity);
        return total - (total * discountPercent / 100);
    }

    // One line in products.txt, example: Serum|1003|Niacinamide Serum|The Ordinary|590.0|18
    public String toFileString() {
        return getCategory() + "|" + id + "|" + name + "|" + brand + "|" + price + "|" + stock;
    }

    // Shown inside the product drop-down list of the Orders tab
    // [POLYMORPHISM - OVERRIDING] replaces toString() from Java's Object class
    @Override
    public String toString() {
        return "#" + id + "  " + name + " - ₱" + String.format("%,.2f", price) + "  (stock: " + stock + ")";
    }

    // Creates the correct subclass object based on the category name.
    // [POLYMORPHISM] the return type is Product, but the real object is a Serum, Mask, ...
    public static Product create(String category, int id, String name, String brand, double price, int stock) {
        switch (category) {
            case "Toner":
                return new Toner(id, name, brand, price, stock);
            case "Serum":
                return new Serum(id, name, brand, price, stock);
            case "Moisturizer":
                return new Moisturizer(id, name, brand, price, stock);
            case "Sunscreen":
                return new Sunscreen(id, name, brand, price, stock);
            case "Exfoliant":
                return new Exfoliant(id, name, brand, price, stock);
            case "Treatment":
                return new Treatment(id, name, brand, price, stock);
            case "Mask":
                return new Mask(id, name, brand, price, stock);
            case "Eye Cream":
                return new EyeCream(id, name, brand, price, stock);
            case "Spot Treatment":
                return new SpotTreatment(id, name, brand, price, stock);
            default:
                return new Cleanser(id, name, brand, price, stock);
        }
    }
}
