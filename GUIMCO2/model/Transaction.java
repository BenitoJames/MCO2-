package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all financial calculations for a single checkout transaction.
 * Handles subtotal, VAT, discounts, payments, and receipt generation.
 * This version includes "what if" scenarios:
 * - Multiple payment methods
 * - Returning receipt as a string
 */
public class Transaction {

    private LocalDateTime timestamp;
    private List<CartItem> cartItems;
    private Customer customer;
    private double subtotal;
    private double seniorDiscountAmount;
    private double pointsDiscountAmount;
    private double vatAmount;
    private double finalTotal;
    private double paymentReceived;
    private int pointsRedeemed;
    private String paymentMethod;

    // Constants as per spec
    private static final double VAT_RATE = 0.12;
    private static final double SENIOR_DISCOUNT = 0.20;

    /**
     * Constructs a Transaction object for a specific customer.
     *
     * @param customer (Customer) The customer involved in this transaction.
     */
    public Transaction(Customer customer) {
        this.timestamp = LocalDateTime.now();
        this.cartItems = new ArrayList<>();
        this.customer = customer;
        this.subtotal = 0;
        this.seniorDiscountAmount = 0;
        this.pointsDiscountAmount = 0;
        this.vatAmount = 0;
        this.finalTotal = 0;
        this.paymentReceived = 0;
        this.pointsRedeemed = 0;
        this.paymentMethod = "N/A";
    }

    /**
     * Adds a CartItem to the transaction.
     * @param item (CartItem) The item to add.
     */
    public void addItem(CartItem item) {
        this.cartItems.add(item);
    }
    
    /**
     * Calculates the subtotal, discounts, VAT, and final total.
     * @param isSenior (boolean) Whether to apply the senior discount.
     */
    public void calculateTotals(boolean isSenior) {
        // 1. Calculate Subtotal
        this.subtotal = 0;
        for (CartItem item : this.cartItems) {
            this.subtotal += item.getSubtotal();
        }

        // 2. Calculate Senior Discount (if applicable)
        if (isSenior) {
            this.seniorDiscountAmount = this.subtotal * SENIOR_DISCOUNT;
        } else {
            this.seniorDiscountAmount = 0;
        }
        
        double discountedTotal = this.subtotal - this.seniorDiscountAmount;

        // 3. Calculate VAT (VAT is based on the price *after* senior discount)
        // We assume the price is VAT-inclusive, so we extract the VAT.
        // VATable sale = discountedTotal / (1 + VAT_RATE)
        // VAT Amount = VATable sale * VAT_RATE
        double vatableSale = discountedTotal / (1 + VAT_RATE);
        this.vatAmount = vatableSale * VAT_RATE;

        // 4. Set Final Total
        this.finalTotal = discountedTotal;
    }

    /**
     * Applies membership points to the transaction, recalculating the final total.
     * @param pointsToUse (int) The number of points to redeem.
     * @return (double) The new finalTotal.
     */
    public double redeemPoints(int pointsToUse) {
        if (this.customer.hasMembership()) {
            double discount = this.customer.getMembershipCard().usePoints(pointsToUse);
            
            // Ensure discount doesn't exceed the total
            if (discount > this.finalTotal) {
                // Refund the difference
                int excessPoints = (int) (discount - this.finalTotal);
                this.customer.getMembershipCard().refundPoints(excessPoints);
                discount = this.finalTotal; // Max discount is the total
                this.pointsRedeemed = pointsToUse - excessPoints;
            } else {
                this.pointsRedeemed = pointsToUse;
            }
            
            this.pointsDiscountAmount = discount;
            this.finalTotal -= this.pointsDiscountAmount;
        }
        return this.finalTotal;
    }

    /**
     * Processes the final payment.
     * @param amount (double) The amount of money received.
     * @param method (String) The payment method (e.g., "Cash", "Card").
     * @return (double) The change to be given, or -1 if payment is insufficient.
     */
    public double processPayment(double amount, String method) {
        this.paymentReceived = amount;
        this.paymentMethod = method;
        double change = -1;

        if (this.paymentReceived >= this.finalTotal) {
            change = this.paymentReceived - this.finalTotal;
        }
        
        return change;
    }
    
    /**
     * Returns the amount eligible for earning points (Total before points were redeemed).
     * @return (double) The total amount spent.
     */
    public double getAmountForPointsEarning() {
        // Points are earned on the final total *before* point redemption
        return this.finalTotal + this.pointsDiscountAmount;
    }
    
    /**
     * Returns a string summary of the totals for display.
     * @return (String) A formatted string of the totals.
     */
    public String getTotalsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Transaction Totals ---\n");
        sb.append(String.format("Subtotal: ₱%.2f\n", this.subtotal));
        if (this.seniorDiscountAmount > 0) {
            sb.append(String.format("Senior Discount (20%%): -₱%.2f\n", this.seniorDiscountAmount));
        }
        sb.append(String.format("VAT (12%% included): ₱%.2f\n", this.vatAmount));
        sb.append("------------------------------------\n");
        sb.append(String.format("Total Due: ₱%.2f\n", this.finalTotal));
        return sb.toString();
    }

    /**
     * Generates a full, formatted receipt as a single string.
     * @return (String) The receipt.
     */
    public String getReceiptString() {
        StringBuilder sb = new StringBuilder();
        String dateTime = this.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sb.append("====================================\n");
        sb.append("      DLSU CONVENIENCE STORE      \n");
        sb.append("====================================\n");
        sb.append("Date/Time: ").append(dateTime).append("\n");
        sb.append("Customer: ").append(this.customer.getName()).append("\n");
        if (this.customer.getIsSenior()) {
            sb.append("Status: Senior/PWD\n");
        }
        sb.append("------------------------------------\n");
        sb.append("Items:\n");
        for (CartItem item : this.cartItems) {
            sb.append(item.toString()).append("\n");
        }
        sb.append("------------------------------------\n");
        sb.append(String.format("Subtotal: ₱%.2f\n", this.subtotal));

        if (this.seniorDiscountAmount > 0) {
            sb.append(String.format("Senior Discount: -₱%.2f\n", this.seniorDiscountAmount));
        }
        if (this.pointsDiscountAmount > 0) {
            sb.append(String.format("Points Redeemed: -₱%.2f\n", this.pointsDiscountAmount));
        }

        sb.append(String.format("VAT (12%%): ₱%.2f\n", this.vatAmount));
        sb.append("------------------------------------\n");
        sb.append(String.format("TOTAL DUE: ₱%.2f\n", this.finalTotal));
        sb.append(String.format("AMOUNT PAID: ₱%.2f\n", this.paymentReceived));
        sb.append(String.format("CHANGE: ₱%.2f\n", this.paymentReceived - this.finalTotal));
        sb.append("Payment Method: ").append(this.paymentMethod).append("\n");
        sb.append("====================================\n");

        return sb.toString();
    }

    /**
     * Prints the receipt string to the console.
     */
    public void printReceipt() {
        System.out.println(this.getReceiptString());
    }

    /**
     * Generates a one-line summary for the sales log.
     * @return (String) A CSV-formatted summary string.
     */
    public String getTransactionSummary() {
        String dateTime = this.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String summary = String.format("%s,%s,%.2f,%s",
                dateTime,
                this.customer.getCustomerID(),
                this.finalTotal,
                this.paymentMethod);
        return summary;
    }

    // --- Getters ---
    public double getFinalTotal() {
        return finalTotal;
    }
}

