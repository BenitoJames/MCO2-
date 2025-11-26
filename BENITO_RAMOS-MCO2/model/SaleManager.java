package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all product sales/discounts in the store.
 */
public class SaleManager {
    private List<ProductSale> sales;
    private int nextSaleID;
    
    public SaleManager() {
        this.sales = new ArrayList<>();
        this.nextSaleID = 1;
    }
    
    /**
     * Adds a new sale.
     */
    public ProductSale addSale(String targetID, String discountType, double discountValue,
                               LocalDateTime startDate, LocalDateTime endDate) {
        String saleID = "SALE-" + String.format("%04d", nextSaleID++);
        ProductSale sale = new ProductSale(saleID, targetID, discountType, discountValue, startDate, endDate);
        sales.add(sale);
        return sale;
    }
    
    /**
     * Removes a sale by ID.
     */
    public boolean removeSale(String saleID) {
        return sales.removeIf(sale -> sale.getSaleID().equals(saleID));
    }
    
    /**
     * Ends a sale (marks as inactive).
     */
    public boolean endSale(String saleID) {
        for (ProductSale sale : sales) {
            if (sale.getSaleID().equals(saleID)) {
                sale.setActive(false);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets a sale by ID.
     */
    public ProductSale getSale(String saleID) {
        for (ProductSale sale : sales) {
            if (sale.getSaleID().equals(saleID)) {
                return sale;
            }
        }
        return null;
    }
    
    /**
     * Gets all sales.
     */
    public List<ProductSale> getAllSales() {
        return new ArrayList<>(sales);
    }
    
    /**
     * Gets all active sales.
     */
    public List<ProductSale> getActiveSales() {
        List<ProductSale> activeSales = new ArrayList<>();
        for (ProductSale sale : sales) {
            if (sale.isCurrentlyActive()) {
                activeSales.add(sale);
            }
        }
        return activeSales;
    }
    
    /**
     * Gets the best applicable sale for a product.
     * Returns the sale with the highest discount amount.
     */
    public ProductSale getBestSaleForProduct(Product product) {
        ProductSale bestSale = null;
        double maxDiscount = 0;
        
        for (ProductSale sale : sales) {
            if (sale.appliesTo(product)) {
                double discount = sale.getDiscountAmount(product.getPrice());
                if (discount > maxDiscount) {
                    maxDiscount = discount;
                    bestSale = sale;
                }
            }
        }
        
        return bestSale;
    }
    
    /**
     * Gets the discounted price for a product (considering best applicable sale).
     */
    public double getDiscountedPrice(Product product) {
        ProductSale bestSale = getBestSaleForProduct(product);
        if (bestSale != null) {
            return bestSale.calculateDiscountedPrice(product.getPrice());
        }
        return product.getPrice();
    }
    
    /**
     * Checks if a product has any active sale.
     */
    public boolean hasActiveSale(Product product) {
        return getBestSaleForProduct(product) != null;
    }
    
    /**
     * Clears all expired sales.
     */
    public int clearExpiredSales() {
        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        List<ProductSale> toRemove = new ArrayList<>();
        
        for (ProductSale sale : sales) {
            if (now.isAfter(sale.getEndDate())) {
                toRemove.add(sale);
                count++;
            }
        }
        
        sales.removeAll(toRemove);
        return count;
    }
    
    /**
     * Updates the next sale ID counter (used when loading from file).
     */
    public void updateNextSaleID(int newNextID) {
        if (newNextID > this.nextSaleID) {
            this.nextSaleID = newNextID;
        }
    }
}
