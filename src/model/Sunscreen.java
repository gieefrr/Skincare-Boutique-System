package model;

/**
 * [INHERITANCE] a Sunscreen "is a" Product (Sunscreen extends Product).
 * [POLYMORPHISM - OVERRIDING] It also overrides computeTotal() to give a promo: 5% off when buying 3 or more.
 */
public class Sunscreen extends Product {   // the only product with a promo

    public static final int PROMO_QUANTITY = 3;

    public Sunscreen(int id, String name, String brand, double price, int stock) {   // made in addSampleProducts() and Product.create()
        super(id, name, brand, price, stock);   // [INHERITANCE] calls the Product constructor
    }

    // [POLYMORPHISM - OVERRIDING] this class gives its own answer to each abstract method of Product
    @Override
    public String getCategory() {
        return "Sunscreen";
    }

    @Override
    public String getIngredients() {
        return "Zinc oxide, titanium dioxide, avobenzone";
    }

    @Override
    public String getBestFor() {
        return "All skin types";
    }

    @Override
    public String getBenefits() {
        return "Protects from UV rays, prevents early aging and skin cancer";
    }

    // [POLYMORPHISM - OVERRIDING] replaces Product's computeTotal() to add the promo
    @Override
    public double computeTotal(int quantity) {
        double total = super.computeTotal(quantity);   // [INHERITANCE] uses the parent (Product) version first
        if (quantity >= PROMO_QUANTITY) {
            total = total * 0.95;
        }
        return total;
    }
}