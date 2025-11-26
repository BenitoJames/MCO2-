package model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Manages the store's inventory, which is split into two shelves:
 * one for perishable goods and one for non-perishable goods.
 * This version removes the obsolete reduceStockFromCart method.
 */
public class Inventory {

    private Shelf perishableShelf;
    private Shelf nonPerishableShelf;
    
    // This is the low-stock threshold we discussed
    private static final int LOW_STOCK_THRESHOLD = 5;

    /**
     * Constructs a new Inventory, initializing both shelves.
     */
    public Inventory() {
        this.perishableShelf = new Shelf();
        this.nonPerishableShelf = new Shelf();
    }

    /**
     * Adds a product to the correct shelf based on its type.
     *
     * @param product (Product) The product to add.
     */
    public void addProduct(Product product) {
        if (product instanceof PerishableProduct) {
            this.perishableShelf.addProduct(product);
        } else {
            this.nonPerishableShelf.addProduct(product);
        }
    }

    /**
     * Finds a product by its ID by searching both shelves.
     *
     * @param productID (String) The ID of the product to find.
     * @return (Product) The found product, or null if not found.
     */
    public Product findProductByID(String productID) {
        Product product = this.perishableShelf.findProductByID(productID);
        if (product == null) {
            product = this.nonPerishableShelf.findProductByID(productID);
        }
        return product;
    }

    /**
     * Generates a string displaying the contents of both shelves.
     * Products are sorted alphabetically by ID, then numerically.
     *
     * @return (String) A formatted string of all products.
     */
    public String viewAllInventory() {
        StringJoiner joiner = new StringJoiner("\n");
        
        // Get sorted products from both shelves
        List<Product> perishableSorted = getSortedProducts(this.perishableShelf.getProducts());
        List<Product> nonPerishableSorted = getSortedProducts(this.nonPerishableShelf.getProducts());
        
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
     * Gets a list of all items from both shelves that are at or below the low stock threshold.
     *
     * @return (List<Product>) A list of low stock products.
     */
    public List<Product> getLowStockItems() {
        List<Product> lowStockItems = new ArrayList<>();
        lowStockItems.addAll(this.perishableShelf.getLowStockItems(LOW_STOCK_THRESHOLD));
        lowStockItems.addAll(this.nonPerishableShelf.getLowStockItems(LOW_STOCK_THRESHOLD));
        return lowStockItems;
    }

   /**
     * Gets a list of all perishable items that are expiring soon or expired.
     *
     * @return (List<Product>) A list of expiring products.
     */
    public List<Product> getExpiringItems() {
        // Only the perishable shelf can have expiring items
        return this.perishableShelf.getExpiringItems();
    }

    /**
     * Removes all expired or soon-to-be-expired items from the inventory.
     *
     * @return (List<Product>) A list of the products that were removed.
     */
    public List<Product> removeExpiringItems() {
        // Only the perishable shelf can have expiring items
        return this.perishableShelf.removeExpiringItems();
    } 

    /**
     * Gathers all products from both shelves into a single list.
     * This is used for saving the inventory to a file.
     *
     * @return (List<Product>) A complete list of all products in the inventory.
     */
    public List<Product> getAllProducts() {
        List<Product> allProducts = new ArrayList<>();
        allProducts.addAll(this.perishableShelf.getProducts());
        allProducts.addAll(this.nonPerishableShelf.getProducts());
        return allProducts;
    }
}

