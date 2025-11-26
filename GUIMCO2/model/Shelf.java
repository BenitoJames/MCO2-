package model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.time.LocalDate;

/**
 * Represents a shelf in the convenience store that stores products.
 */
public class Shelf {

    private ArrayList<Product> products;

    /**
     * Constructs a new, empty Shelf.
     */
    public Shelf() {
        this.products = new ArrayList<Product>();
    }

    /**
     * Adds a product to this shelf.
     *
     * @param product (Product) The product to add.
     */
    public void addProduct(Product product) {
        this.products.add(product);
    }

    /**
     * Returns the complete list of products on this shelf.
     *
     * @return (List<Product>) The list of products.
     */
    public List<Product> getProducts() {
        return this.products;
    }

    /**
     * Finds a product by its ID on this shelf.
     *
     * @param productID (String) The ID to search for.
     * @return (Product) The found product, or null.
     */
    public Product findProductByID(String productID) {
        Product foundProduct = null;
        boolean found = false;
        int i = 0;

        while (i < this.products.size() && !found) {
            Product p = this.products.get(i);
            if (p.getProductID().equalsIgnoreCase(productID)) {
                foundProduct = p;
                found = true;
            }
            i++;
        }
        return foundProduct;
    }

    /**
     * Generates a display string for all products on this shelf.
     *
     * @param title (String) The title to display for this shelf (e.g., "Perishable").
     * @return (String) A formatted string of all products.
     */
    public String getDisplayString(String title) {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add("--- " + title + " ---");

        if (this.products.isEmpty()) {
            joiner.add(" (Empty)");
        } else {
            for (Product p : this.products) {
                joiner.add(p.displayDetails());
            }
        }
        return joiner.toString();
    }

    /**
     * Gets a list of items at or below the low stock threshold.
     *
     * @param threshold (int) The low stock threshold.
     * @return (List<Product>) A list of low stock products.
     */
    public List<Product> getLowStockItems(int threshold) {
        List<Product> lowStock = new ArrayList<>();
        for (Product p : this.products) {
            if (p.getQuantityInStock() <= threshold) {
                lowStock.add(p);
            }
        }
        return lowStock;
    }

    /**
     * Gets a list of perishable items that are expiring soon or expired.
     *
     * @return (List<Product>) A list of expiring products.
     */
    public List<Product> getExpiringItems() {
        List<Product> expiring = new ArrayList<>();
        for (Product p : this.products) {
            // Check if it's a PerishableProduct
            if (p instanceof PerishableProduct) {
                PerishableProduct pp = (PerishableProduct) p;
                if (pp.isFlaggedForRemoval()) {
                    expiring.add(pp);
                }
            }
        }
        return expiring;
    }

    /**
     * Removes all expired or soon-to-be-expired items from this shelf.
     *
     * @return (List<Product>) A list of the products that were removed.
     */
    public List<Product> removeExpiringItems() {
        List<Product> removed = new ArrayList<>();
        // We must use a separate list and removeAll to avoid ConcurrentModificationException
        List<Product> toRemove = new ArrayList<>();

        for (Product p : this.products) {
            if (p instanceof PerishableProduct) {
                if (((PerishableProduct) p).isFlaggedForRemoval()) {
                    toRemove.add(p);
                }
            }
        }

        this.products.removeAll(toRemove);
        removed.addAll(toRemove);
        return removed;
    }
}

