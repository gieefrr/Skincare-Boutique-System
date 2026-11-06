package model;

/**
 * [INHERITANCE] a Treatment "is a" Product (Treatment extends Product).
 */
public class Treatment extends Product {

    public Treatment(int id, String name, String brand, double price, int stock) {   // made in addSampleProducts() and Product.create()
        super(id, name, brand, price, stock);   // [INHERITANCE] calls the Product constructor
    }

    // [POLYMORPHISM - OVERRIDING] this class gives its own answer to each abstract method of Product
    @Override
    public String getCategory() {
        return "Treatment";
    }

    @Override
    public String getIngredients() {
        return "Retinoids, azelaic acid, benzoyl peroxide";
    }

    @Override
    public String getBestFor() {
        return "Acne, pigmentation and wrinkles";
    }

    @Override
    public String getBenefits() {
        return "Boosts cell turnover, reduces acne, fades dark spots";
    }
}