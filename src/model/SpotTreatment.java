package model;

/**
 * [INHERITANCE] a SpotTreatment "is a" Product (SpotTreatment extends Product).
 */
public class SpotTreatment extends Product {

    public SpotTreatment(int id, String name, String brand, double price, int stock) {   // made in addSampleProducts() and Product.create()
        super(id, name, brand, price, stock);   // [INHERITANCE] calls the Product constructor
    }

    // [POLYMORPHISM - OVERRIDING] this class gives its own answer to each abstract method of Product
    @Override
    public String getCategory() {
        return "Spot Treatment";
    }

    @Override
    public String getIngredients() {
        return "Benzoyl peroxide, sulfur, salicylic acid";
    }

    @Override
    public String getBestFor() {
        return "Acne-prone skin";
    }

    @Override
    public String getBenefits() {
        return "Targets pimples directly, reduces inflammation";
    }
}