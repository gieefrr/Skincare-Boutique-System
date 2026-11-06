package model;

/**
 * [INHERITANCE] a Moisturizer "is a" Product (Moisturizer extends Product).
 */
public class Moisturizer extends Product {

    public Moisturizer(int id, String name, String brand, double price, int stock) {   // made in addSampleProducts() and Product.create()
        super(id, name, brand, price, stock);   // [INHERITANCE] calls the Product constructor
    }

    // [POLYMORPHISM - OVERRIDING] this class gives its own answer to each abstract method of Product
    @Override
    public String getCategory() {
        return "Moisturizer";
    }

    @Override
    public String getIngredients() {
        return "Ceramides, squalane, glycerin, shea butter";
    }

    @Override
    public String getBestFor() {
        return "Dry, sensitive and normal skin";
    }

    @Override
    public String getBenefits() {
        return "Locks in hydration, repairs the skin barrier, prevents water loss";
    }
}