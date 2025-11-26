package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a sale/discount on a product or category.
 * Can be percentage-based or fixed amount discount.
 */
public class ProductSale {
    private String saleID;
    private String targetID; // Product ID or category code (e.g., "ALL-F" for all food)
    private String discountType; // "PERCENTAGE" or "FIXED"
    private double discountValue; // Percentage (0-100) or fixed amount
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;
    
    /**
     * Constructs a ProductSale.
     */
    public ProductSale(String saleID, String targetID, String discountType, double discountValue, 
                       LocalDateTime startDate, LocalDateTime endDate) {
        this.saleID = saleID;
        this.targetID = targetID;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = true;
    }
    
    /**
     * Checks if the sale is currently active based on date/time.
     */
    public boolean isCurrentlyActive() {
        if (!isActive) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }
    
    /**
     * Calculates the discounted price for a given original price.
     */
    public double calculateDiscountedPrice(double originalPrice) {
        if (!isCurrentlyActive()) {
            return originalPrice;
        }
        
        if (discountType.equals("PERCENTAGE")) {
            return originalPrice * (1 - discountValue / 100.0);
        } else { // FIXED
            double discounted = originalPrice - discountValue;
            return Math.max(0, discounted); // Don't go below 0
        }
    }
    
    /**
     * Gets the discount amount for a given original price.
     */
    public double getDiscountAmount(double originalPrice) {
        return originalPrice - calculateDiscountedPrice(originalPrice);
    }
    
    /**
     * Checks if this sale applies to a given product.
     */
    public boolean appliesTo(Product product) {
        if (!isCurrentlyActive()) {
            return false;
        }
        
        // Direct product ID match
        if (targetID.equals(product.getProductID())) {
            return true;
        }
        
        // Category match (e.g., "ALL-F" for all food)
        if (targetID.startsWith("ALL-")) {
            String categoryPrefix = targetID.substring(4); // Get the letter after "ALL-"
            return product.getProductID().startsWith(categoryPrefix + "-");
        }
        
        return false;
    }
    
    /**
     * Returns a formatted display string for the sale.
     */
    public String getDisplayString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String discountStr = discountType.equals("PERCENTAGE") 
            ? String.format("%.1f%%", discountValue)
            : String.format("₱%.2f", discountValue);
        
        return String.format("%s | Target: %s | Discount: %s | %s to %s | %s",
            saleID,
            targetID,
            discountStr,
            startDate.format(formatter),
            endDate.format(formatter),
            isCurrentlyActive() ? "ACTIVE" : "INACTIVE"
        );
    }
    
    // Getters and Setters
    
    public String getSaleID() {
        return saleID;
    }
    
    public void setSaleID(String saleID) {
        this.saleID = saleID;
    }
    
    public String getTargetID() {
        return targetID;
    }
    
    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }
    
    public String getDiscountType() {
        return discountType;
    }
    
    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }
    
    public double getDiscountValue() {
        return discountValue;
    }
    
    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}
