package model;

import java.util.List;
import java.util.StringJoiner;

/**
 * The abstract base class for all products sold in the store.
 * Contains common attributes including name, price, stock, brand, and variant.
 */
public abstract class Product {

    private String productID;
    private String name;
    private double price;
    private int quantityInStock;
    private List<String> subcategories;
    private String brand;   // MOVED UP from NonPerishableProduct
    private String variant; // MOVED UP from NonPerishableProduct

    /**
     * Constructs a new Product.
     *
     * @param productID       (String) The unique ID for the product.
     * @param name            (String) The display name for the product.
     * @param price           (double) The retail price of the product.
     * @param quantityInStock (int) The number of units in stock.
     * @param subcategories   (List<String>) A list of category tags.
     * @param brand           (String) The brand name of the product (can be null or empty).
     * @param variant         (String) The variant of the product (e.g., "Zero", "500ml", can be null or empty).
     */
    public Product(String productID, String name, double price, int quantityInStock, List<String> subcategories, String brand, String variant) {
        this.productID = productID;
        this.name = name;
        this.price = price;
        this.quantityInStock = quantityInStock;
        this.subcategories = subcategories;
        this.brand = (brand == null) ? "" : brand; // Ensure not null
        this.variant = (variant == null) ? "" : variant; // Ensure not null
    }

    /**
     * Checks if the item's stock is low (5 or less).
     *
     * @return (boolean) true if quantity is <= 5, false otherwise.
     */
    public boolean isLowStock() {
        return this.quantityInStock <= 5;
    }

    /**
     * Reduces the stock quantity by a specified amount.
     *
     * @param quantity (int) The amount to sell.
     * @return (boolean) true if stock was sufficient and reduced, false otherwise.
     */
    public boolean reduceStock(int quantity) {
        boolean isSuccess = false;
        if (this.quantityInStock >= quantity) {
            this.quantityInStock -= quantity;
            isSuccess = true;
        }
        return isSuccess;
    }

    /**
     * Generates a detailed display string for the product, including brand and variant.
     * This method is now concrete and can be overridden by children.
     *
     * @return (String) A formatted string with product details.
     */
    public String displayDetails() {
        String details;
        String brandAndVariant = getBrandAndVariant();

        details = String.format("ID: %s | %s %s | Price: P%.2f | Stock: %d",
                this.productID,
                this.name,
                brandAndVariant.isEmpty() ? "" : "(" + brandAndVariant + ")",
                this.price,
                this.quantityInStock
        );
        
        details += " | Categories: [" + getSubcategoriesString() + "]";
        
        return details;
    }
    
    /**
     * Helper method to get a single, comma-separated string of subcategories.
     *
     * @return (String) e.g., "Food, Ready-to-eat, Sandwich"
     */
    public String getSubcategoriesString() {
        StringJoiner joiner = new StringJoiner(", ");
        if (this.subcategories != null) {
            for (String s : this.subcategories) {
                joiner.add(s);
            }
        }
        return joiner.toString();
    }
    
    /**
     * Helper method to combine brand and variant into a single, clean string.
     *
     * @return (String) e.g., "Coke (Zero)", "Dove", "1L"
     */
    public String getBrandAndVariant() {
        String result;
        boolean hasBrand = (this.brand != null && !this.brand.isEmpty());
        boolean hasVariant = (this.variant != null && !this.variant.isEmpty());

        if (hasBrand && hasVariant) {
            result = this.brand + " (" + this.variant + ")";
        } else if (hasBrand) {
            result = this.brand;
        } else if (hasVariant) {
            result = this.variant;
        } else {
            result = "";
        }
        return result;
    }

    // --- Getters and Setters ---

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public List<String> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<String> subcategories) {
        this.subcategories = subcategories;
    }
    
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }
}

