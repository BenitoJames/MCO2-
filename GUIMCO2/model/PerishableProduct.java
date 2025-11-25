package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * An abstract subclass for products that can expire.
 * Extends Product and adds an expiration date.
 */
public abstract class PerishableProduct extends Product {

    private LocalDate expirationDate;

    /**
     * Constructs a new PerishableProduct.
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
    public PerishableProduct(String productID, String name, double price, int quantityInStock, List<String> subcategories, String brand, String variant, LocalDate expirationDate) {
        // Pass all base attributes (including brand/variant) to the super class
        super(productID, name, price, quantityInStock, subcategories, brand, variant);
        this.expirationDate = expirationDate;
    }

    /**
     * Checks if the product is expired as of today.
     *
     * @return (boolean) true if the current date is after the expiration date.
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(this.expirationDate);
    }

    /**
     * Checks if the item should be flagged for removal (expiring within 7 days or already expired).
     *
     * @return (boolean) true if expiring in 7 days or less, or already expired.
     */
    public boolean isFlaggedForRemoval() {
        LocalDate oneWeekFromNow = LocalDate.now().plusDays(7);
        // Check if expiration date is on or before 7 days from now, OR it is already expired.
        boolean isExpiringSoon = (this.expirationDate.isBefore(oneWeekFromNow) || this.expirationDate.isEqual(oneWeekFromNow));
        return isExpiringSoon || isExpired();
    }

    /**
     * Generates a detailed display string, appending the expiration date to the base details.
     *
     * @return (String) A formatted string with all product details including expiration date.
     */
    @Override
    public String displayDetails() {
        // Get the base details from Product (ID, name, brand, variant, price, stock, categories)
        String baseDetails = super.displayDetails();
        
        // Append the expiration date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String formattedDate = this.expirationDate.format(formatter);
        
        String details = baseDetails + " | Expires: " + formattedDate;
        
        if (isExpired()) {
            details += " (EXPIRED)";
        } else if (isFlaggedForRemoval()) {
            details += " (REMOVAL_FLAG)";
        }
        
        return details;
    }

    // --- Getters and Setters ---

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}

