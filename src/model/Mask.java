package model;

/**
 * [INHERITANCE] a Mask "is a" Product (Mask extends Product).
 */
public class Mask extends Product {

    public Mask(int id, String name, String brand, double price, int stock) {   // made in addSampleProducts() and Product.create()
        super(id, name, brand, price, stock);   // [INHERITANCE] calls the Product constructor
    }

    // [POLYMORPHISM - OVERRIDING] this class gives its own answer to each abstract method of Product
    @Override
    public String getCategory() {
        return "Mask";
    }

    @Override
    public String getIngredients() {
        return "Clay, charcoal, aloe vera, vitamin E";
    }

    @Override
    public String getBestFor() {
        return "Oily, dry and stressed skin";
    }

    @Override
    public String getBenefits() {
        return "Deep cleanses, hydrates, soothes, brightens";
    }
}