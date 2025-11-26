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
    private double change;
    private int pointsRedeemed;
    private String paymentMethod;
    private String seniorPWDCardID;

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
        this.change = 0;
        this.pointsRedeemed = 0;
        this.paymentMethod = "N/A";
        this.seniorPWDCardID = null;
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
     * For Senior/PWD: Remove VAT first, then apply 20% discount on the entire order.
     * @param isSeniorOrPWD (boolean) Whether to apply the senior/PWD discount.
     */
    public void calculateTotals(boolean isSeniorOrPWD) {
        // 1. Calculate Subtotal
        this.subtotal = 0;
        for (CartItem item : this.cartItems) {
            this.subtotal += item.getSubtotal();
        }

        if (isSeniorOrPWD) {
            // Senior/PWD Discount Logic:
            // Step 1: Remove VAT from subtotal
            // If price is VAT-inclusive: VATable Sale = Subtotal / 1.12
            double vatableSale = this.subtotal / (1 + VAT_RATE);
            this.vatAmount = 0; // VAT is removed for Senior/PWD
            
            // Step 2: Apply 20% discount on the VAT-exclusive amount
            this.seniorDiscountAmount = vatableSale * SENIOR_DISCOUNT;
            this.finalTotal = vatableSale - this.seniorDiscountAmount;
        } else {
            // Regular Customer: VAT is included in the price
            this.seniorDiscountAmount = 0;
            
            // Extract VAT for display purposes
            double vatableSale = this.subtotal / (1 + VAT_RATE);
            this.vatAmount = vatableSale * VAT_RATE;
            
            this.finalTotal = this.subtotal;
        }
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

        if (this.paymentReceived >= this.finalTotal) {
            this.change = this.paymentReceived - this.finalTotal;
        } else {
            this.change = -1;
        }
        
        return this.change;
    }
    
    /**
     * Sets the points discount amount.
     * @param discount (double) The discount amount from points redemption.
     */
    public void setPointsDiscount(double discount) {
        this.pointsDiscountAmount = discount;
        this.finalTotal -= discount;
    }
    
    /**
     * Sets the payment method.
     * @param method (String) The payment method.
     */
    public void setPaymentMethod(String method) {
        this.paymentMethod = method;
    }
    
    /**
     * Sets the amount paid.
     * @param amount (double) The amount paid.
     */
    public void setAmountPaid(double amount) {
        this.paymentReceived = amount;
    }
    
    /**
     * Sets the change amount.
     * @param change (double) The change amount.
     */
    public void setChange(double change) {
        this.change = change;
    }
    
    /**
     * Sets the Senior/PWD card ID for the transaction.
     * @param cardID (String) The Senior/PWD card ID.
     */
    public void setSeniorPWDCardID(String cardID) {
        this.seniorPWDCardID = cardID;
    }
    
    /**
     * Sets the number of points redeemed.
     * @param points (int) The number of points redeemed.
     */
    public void setPointsRedeemed(int points) {
        this.pointsRedeemed = points;
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
        if (this.seniorPWDCardID != null) {
            sb.append("Senior/PWD Card: ").append(this.seniorPWDCardID).append("\n");
        }
        sb.append("------------------------------------\n");
        sb.append("Items:\n");
        for (CartItem item : this.cartItems) {
            sb.append(item.toString()).append("\n");
        }
        sb.append("------------------------------------\n");
        sb.append(String.format("Subtotal: ₱%.2f\n", this.subtotal));

        if (this.seniorDiscountAmount > 0) {
            sb.append("VAT Removed (Senior/PWD)\n");
            sb.append(String.format("Senior/PWD Discount (20%%): -₱%.2f\n", this.seniorDiscountAmount));
        } else {
            sb.append(String.format("VAT (12%% included): ₱%.2f\n", this.vatAmount));
        }
        
        if (this.pointsDiscountAmount > 0) {
            sb.append(String.format("Points Redeemed (%d pts): -₱%.2f\n", 
                this.pointsRedeemed, this.pointsDiscountAmount));
        }

        sb.append("------------------------------------\n");
        sb.append(String.format("TOTAL DUE: ₱%.2f\n", this.finalTotal));
        sb.append(String.format("AMOUNT PAID: ₱%.2f\n", this.paymentReceived));
        sb.append(String.format("CHANGE: ₱%.2f\n", this.change));
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
    
    public double getSubtotal() {
        return subtotal;
    }
    
    public List<CartItem> getCartItems() {
        return cartItems;
    }
}

