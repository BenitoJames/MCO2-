package model;

import java.util.List;

/**
 * An abstract subclass for products that do not expire.
 * This class is now a "marker" class, as brand/variant are handled by the base Product class.
 */
public abstract class NonPerishableProduct extends Product {

    /**
     * Constructs a new NonPerishableProduct.
     *
     * @param productID       (String) The unique ID for the product.
     * @param name            (String) The display name for the product.
     * @param price           (double) The retail price of the product.
     * @param quantityInStock (int) The number of units in stock.
     * @param subcategories   (List<String>) A list of category tags.
     * @param brand           (String) The brand name of the product.
     * @param variant         (String) The variant of the product.
     */
    public NonPerishableProduct(String productID, String name, double price, int quantityInStock, List<String> subcategories, String brand, String variant) {
        // Pass all attributes (including brand/variant) to the super class
        super(productID, name, price, quantityInStock, subcategories, brand, variant);
        // This class no longer needs to store brand/variant itself.
    }

    /**
     * Generates a detailed display string.
     * This method just calls the super implementation, as NonPerishableProduct
     * adds no new details.
     *
     * @return (String) A formatted string with all product details.
     */
    @Override
    public String displayDetails() {
        // The base class displayDetails() already includes brand/variant.
        return super.displayDetails();
    }
}

