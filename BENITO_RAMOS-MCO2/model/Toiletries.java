package model;

import java.util.List;

/**
 * Represents a Toiletry product.
 * This is a concrete class that extends NonPerishableProduct.
 */
public class Toiletries extends NonPerishableProduct {

    /**
     * Constructs a new Toiletry object.
     *
     * @param productID       The unique identifier for the product
     * @param name            The name of the product
     * @param price           The base price of the product
     * @param quantityInStock The starting quantity in stock
     * @param subcategories   A List of strings for its categories
     * @param brand           The brand of the product
     * @param variant         The variant of the product
     */
    public Toiletries(String productID, String name, double price, int quantityInStock, List<String> subcategories, String brand, String variant) {
        // Pass all arguments up to the NonPerishableProduct constructor
        super(productID, name, price, quantityInStock, subcategories, brand, variant);
    }
    
    // This class inherits all methods from NonPerishableProduct and Product.
}

