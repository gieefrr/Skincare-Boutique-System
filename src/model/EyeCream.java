package model;

/**
 * [INHERITANCE] an EyeCream "is a" Product (EyeCream extends Product).
 */
public class EyeCream extends Product {

    public EyeCream(int id, String name, String brand, double price, int stock) {   // made in addSampleProducts() and Product.create()
        super(id, name, brand, price, stock);   // [INHERITANCE] calls the Product constructor
    }

    // [POLYMORPHISM - OVERRIDING] this class gives its own answer to each abstract method of Product
    @Override
    public String getCategory() {
        return "Eye Cream";
    }
    @Override
    public String getIngredients() {
        return "Caffeine, peptides, hyaluronic acid";
    }
    @Override
    public String getBestFor() {
        return "Dark circles, puffiness and fine lines";
    }
    @Override
    public String getBenefits() {
        return "Reduces puffiness, hydrates delicate skin, firms";
    }
}