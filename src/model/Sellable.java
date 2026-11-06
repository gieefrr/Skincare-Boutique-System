package model;

/**
 * [ABSTRACTION] INTERFACE - anything that can be sold must know how to compute its total price.
 * It only says WHAT to do; Product (which implements it) says HOW.
 * [POLYMORPHISM - OVERLOADING] the two methods have the same name but different parameters.
 */
public interface Sellable {   // Product is the one that does the actual computing

    double computeTotal(int quantity);

    double computeTotal(int quantity, double discountPercent);
}
