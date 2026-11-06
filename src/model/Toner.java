package model;

/**
 * [INHERITANCE] a Toner "is a" Product (Toner extends Product).
 */
public class Toner extends Product {

    public Toner(int id, String name, String brand, double price, int stock) {   // made in addSampleProducts() and Product.create()
        super(id, name, brand, price, stock);   // [INHERITANCE] calls the Product constructor
    }

    // [POLYMORPHISM - OVERRIDING] this class gives its own answer to each abstract method of Product
    @Override
    public String getCategory() {
        return "Toner";
    }

    @Override
    public String getIngredients() {
        return "Niacinamide, witch hazel, hyaluronic acid";
    }

    @Override
    public String getBestFor() {
        return "Oily, dehydrated and dull skin";
    }

    @Override
    public String getBenefits() {
        return "Balances pH, refines pores, hydrates, preps skin for serums";
    }
}