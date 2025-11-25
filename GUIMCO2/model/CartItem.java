package model;
/**
 * Represents a single item (a product and its quantity) in a shopping cart.
 */
public class CartItem {

    private Product product;
    private int quantity;

    /**
     * Constructs a CartItem.
     *
     * @param product  (Product) The product being added.
     * @param quantity (int) The quantity of that product.
     */
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    /**
     * Gets the Product object.
     * @return (Product) The product.
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Gets the quantity of the product.
     * @return (int) The quantity.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the product.
     * @param quantity (int) The new quantity.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Computes the subtotal (price * quantity) for this item.
     * @return (double) The computed subtotal.
     */
    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    /**
     * Returns a formatted string representation of this cart item.
     * Used for displaying in the cart and on the receipt.
     *
     * @return (String) A descriptive text of the cart item.
     */
    @Override
    public String toString() {
        // Format: "Qty  Name      (PPrice_ea)     PSubtotal"
        // e.g.,   "2x   Coke      (P50.00 ea)   P100.00"
        String line1 = String.format("%-3s %-15s", 
                            this.quantity + "x", 
                            this.product.getName());
        String line2 = String.format("(P%.2f ea)   P%.2f",
                            this.product.getPrice(),
                            this.getSubtotal());
        return line1 + "\n" + "  " + line2;
    }
}

