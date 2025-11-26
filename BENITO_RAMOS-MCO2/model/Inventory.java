package model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Manages the store's inventory, which is split into two lists:
 * one for perishable goods and one for non-perishable goods.
 */
public class Inventory {

    private List<Product> perishableProducts;
    private List<Product> nonPerishableProducts;
    
    // This is the low-stock threshold we discussed
    private static final int LOW_STOCK_THRESHOLD = 5;

    /**
     * Constructs a new Inventory, initializing both product lists.
     */
    public Inventory() {
        this.perishableProducts = new ArrayList<>();
        this.nonPerishableProducts = new ArrayList<>();
    }

    /**
     * Adds a product to the correct list based on its type.
     *
     * @param product (Product) The product to add.
     */
    public void addProduct(Product product) {
        if (product instanceof PerishableProduct) {
            this.perishableProducts.add(product);
        } else {
            this.nonPerishableProducts.add(product);
        }
    }

    /**
     * Finds a product by its ID by searching both lists.
     *
     * @param productID (String) The ID of the product to find.
     * @return (Product) The found product, or null if not found.
     */
    public Product findProductByID(String productID) {
        for (Product p : perishableProducts) {
            if (p.getProductID().equals(productID)) {
                return p;
            }
        }
        for (Product p : nonPerishableProducts) {
            if (p.getProductID().equals(productID)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Generates a string displaying the contents of both product lists.
     * Products are sorted alphabetically by ID, then numerically.
     *
     * @return (String) A formatted string of all products.
     */
    public String viewAllInventory() {
        StringJoiner joiner = new StringJoiner("\n");
        
        // Get sorted products from both lists
        List<Product> perishableSorted = getSortedProducts(this.perishableProducts);
        List<Product> nonPerishableSorted = getSortedProducts(this.nonPerishableProducts);
        
        joiner.add("--- Perishable ---");
        if (perishableSorted.isEmpty()) {
            joiner.add(" (Empty)");
        } else {
            for (Product p : perishableSorted) {
                joiner.add(p.displayDetails());
            }
        }
        
        joiner.add("\n--- Non-Perishable ---");
        if (nonPerishableSorted.isEmpty()) {
            joiner.add(" (Empty)");
        } else {
            for (Product p : nonPerishableSorted) {
                joiner.add(p.displayDetails());
            }
        }
        
        return joiner.toString();
    }

    /**
     * Sorts products by ID: alphabetically first (by category prefix), then numerically.
     * Example: F-001, F-002, B-001, T-005
     *
     * @param products (List<Product>) The list to sort.
     * @return (List<Product>) A sorted copy of the list.
     */
    private List<Product> getSortedProducts(List<Product> products) {
        List<Product> sorted = new ArrayList<>(products);
        sorted.sort((p1, p2) -> {
            String id1 = p1.getProductID();
            String id2 = p2.getProductID();
            
            // Split by dash to separate prefix and number
            String[] parts1 = id1.split("-");
            String[] parts2 = id2.split("-");
            
            String prefix1 = parts1.length > 0 ? parts1[0] : "";
            String prefix2 = parts2.length > 0 ? parts2[0] : "";
            
            // Compare prefix alphabetically
            int prefixCompare = prefix1.compareTo(prefix2);
            if (prefixCompare != 0) {
                return prefixCompare;
            }
            
            // If same prefix, compare number numerically
            try {
                int num1 = parts1.length > 1 ? Integer.parseInt(parts1[1]) : 0;
                int num2 = parts2.length > 1 ? Integer.parseInt(parts2[1]) : 0;
                return Integer.compare(num1, num2);
            } catch (NumberFormatException e) {
                // If number parsing fails, compare as strings
                return id1.compareTo(id2);
            }
        });
        return sorted;
    }

    /**
     * Gets a list of all items from both lists that are at or below the low stock threshold.
     *
     * @return (List<Product>) A list of low stock products.
     */
    public List<Product> getLowStockItems() {
        List<Product> lowStockItems = new ArrayList<>();
        
        // Check perishable products
        for (Product p : perishableProducts) {
            if (p.getQuantityInStock() <= LOW_STOCK_THRESHOLD) {
                lowStockItems.add(p);
            }
        }
        
        // Check non-perishable products
        for (Product p : nonPerishableProducts) {
            if (p.getQuantityInStock() <= LOW_STOCK_THRESHOLD) {
                lowStockItems.add(p);
            }
        }
        
        return lowStockItems;
    }

   /**
     * Gets a list of all perishable items that are expiring soon or expired.
     *
     * @return (List<Product>) A list of expiring products.
     */
    public List<Product> getExpiringItems() {
        List<Product> expiringItems = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate soonThreshold = today.plusDays(3);
        
        for (Product p : perishableProducts) {
            if (p instanceof PerishableProduct) {
                PerishableProduct pp = (PerishableProduct) p;
                if (pp.getExpirationDate().isBefore(soonThreshold) || pp.getExpirationDate().isEqual(soonThreshold)) {
                    expiringItems.add(pp);
                }
            }
        }
        
        return expiringItems;
    }

    /**
     * Removes all expired or soon-to-be-expired items from the inventory.
     *
     * @return (List<Product>) A list of the products that were removed.
     */
    public List<Product> removeExpiringItems() {
        List<Product> removed = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate soonThreshold = today.plusDays(3);
        
        List<Product> toRemove = new ArrayList<>();
        for (Product p : perishableProducts) {
            if (p instanceof PerishableProduct) {
                PerishableProduct pp = (PerishableProduct) p;
                if (pp.getExpirationDate().isBefore(soonThreshold) || pp.getExpirationDate().isEqual(soonThreshold)) {
                    toRemove.add(pp);
                }
            }
        }
        
        perishableProducts.removeAll(toRemove);
        removed.addAll(toRemove);
        
        return removed;
    } 

    /**
     * Gathers all products from both lists into a single list.
     * This is used for saving the inventory to a file.
     *
     * @return (List<Product>) A complete list of all products in the inventory.
     */
    public List<Product> getAllProducts() {
        List<Product> allProducts = new ArrayList<>();
        allProducts.addAll(this.perishableProducts);
        allProducts.addAll(this.nonPerishableProducts);
        return allProducts;
    }
}

