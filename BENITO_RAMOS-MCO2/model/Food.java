package model;

import java.time.LocalDate;
import java.util.List;

/**
 * Concrete class for Food products.
 * Extends PerishableProduct.
 */
public class Food extends PerishableProduct {

    /**
     * Constructs a new Food product.
     *
     * @param productID       (String) The unique ID for the product.
     * @param name            (String) The display name for the product.
     * @param price           (double) The retail price of the product.
     * @param quantityInStock (int) The number of units in stock.
     * @param subcategories   (List<String>) A list of category tags.
     * @param brand           (String) The brand name of the product.
     * @param variant         (String) The variant of the product.
     * @param expirationDate  (LocalDate) The expiration date.
     */
    public Food(String productID, String name, double price, int quantityInStock, List<String> subcategories, String brand, String variant, LocalDate expirationDate) {
        // Pass all parameters, including brand/variant, to the PerishableProduct constructor
        super(productID, name, price, quantityInStock, subcategories, brand, variant, expirationDate);
    }
}