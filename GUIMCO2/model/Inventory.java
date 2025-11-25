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
     *
     * @return (String) A formatted string of all products.
     */
    public String viewAllInventory() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(this.perishableShelf.getDisplayString("Perishable"));
        joiner.add(this.nonPerishableShelf.getDisplayString("Non-Perishable"));
        return joiner.toString();
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

