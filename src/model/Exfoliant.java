package model;

/**
 * [INHERITANCE] an Exfoliant "is a" Product (Exfoliant extends Product).
 */
public class Exfoliant extends Product {

    public Exfoliant(int id, String name, String brand, double price, int stock) {   // made in addSampleProducts() and Product.create()
        super(id, name, brand, price, stock);   // [INHERITANCE] calls the Product constructor
    }

    // [POLYMORPHISM - OVERRIDING] this class gives its own answer to each abstract method of Product
    @Override
    public String getCategory() {
        return "Exfoliant";
    }

    @Override
    public String getIngredients() {
        return "Glycolic acid, lactic acid, salicylic acid";
    }

    @Override
    public String getBestFor() {
        return "Acne-prone, dull and uneven skin tone";
    }

    @Override
    public String getBenefits() {
        return "Removes dead skin cells, smooths texture, brightens";
    }
}