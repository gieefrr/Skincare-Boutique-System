package model;

/**
 * [INHERITANCE] a Cleanser "is a" Product (Cleanser extends Product).
 */
public class Cleanser extends Product {

    public Cleanser(int id, String name, String brand, double price, int stock) {   // made in addSampleProducts() and Product.create()
        super(
                id, name, brand, price, stock);   // [INHERITANCE] calls the Product constructor
    }

    // [POLYMORPHISM - OVERRIDING] this class gives its own answer to each abstract method of Product
    @Override
    public String getCategory() {
        return "Cleanser";
    }

    @Override
    public String getIngredients() {
        return "Salicylic acid, glycolic acid, ceramides";
    }

    @Override
    public String getBestFor() {
        return "Oily, acne-prone and sensitive skin";
    }

    @Override
    public String getBenefits() {
        return "Removes dirt and oil, unclogs pores, prevents breakouts, keeps the skin barrier healthy";
    }
}