package model;

/**
 * [INHERITANCE] a Serum "is a" Product (Serum extends Product).
 */
public class Serum extends Product {

    public Serum(int id, String name, String brand, double price, int stock) {   // made in addSampleProducts() and Product.create()
        super(id, name, brand, price, stock);   // [INHERITANCE] calls the Product constructor
    }

    // [POLYMORPHISM - OVERRIDING] this class gives its own answer to each abstract method of Product
    @Override
    public String getCategory() {
        return "Serum";
    }

    @Override
    public String getIngredients() {
        return "Vitamin C, hyaluronic acid, peptides, retinol";
    }

    @Override
    public String getBestFor() {
        return "Dark spots, wrinkles and dryness";
    }

    @Override
    public String getBenefits() {
        return "Brightens, hydrates, fights signs of aging, delivers concentrated actives";
    }
}