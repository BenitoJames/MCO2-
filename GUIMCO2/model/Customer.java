package model;
/**
 * Represents a customer in the convenience store.
 * This version includes the customer's name.
 */
public class Customer {

    private String customerID;              // Unique customer ID
    private String name;                    // Customer name
    private MembershipCard membershipCard;  // Optional membership card
    private boolean isSenior;               // True if senior or PWD

    /**
     * Constructs a Customer object with ID and name.
     *
     * @param id   (String) The customer's unique ID.
     * @param name (String) The customer's full name.
     */
    public Customer(String id, String name) {
        this.customerID = id;
        this.name = name;
        this.membershipCard = null;
        this.isSenior = false;
    }

    /**
     * Returns the customer's ID.
     *
     * @return (String) The customer's unique identifier.
     */
    public String getCustomerID() {
        return customerID;
    }

    /**
     * Returns the customer's name.
     *
     * @return (String) The customer's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the customer's name.
     *
     * @param name (String) The new name for the customer.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the customer's membership card.
     *
     * @return (MembershipCard) The customer's membership card, or null if none.
     */
    public MembershipCard getMembershipCard() {
        return membershipCard;
    }

    /**
     * Returns whether the customer is eligible for senior/PWD discount.
     *
     * @return (boolean) True if the customer is a senior/PWD.
     */
    public boolean getIsSenior() {
        return isSenior;
    }


    /**
     * Assigns a membership card to the customer.
     *
     * @param card (MembershipCard) The membership card to assign.
     */
    public void assignMembershipCard(MembershipCard card) {
        this.membershipCard = card;
    }

    /**
     * Sets the senior/PWD status for the customer.
     *
     * @param seniorStatus (boolean) True if the customer is a senior/PWD.
     */
    public void setIsSenior(boolean seniorStatus) {
        this.isSenior = seniorStatus;
    }

    /**
     * Determines if the customer has a membership card.
     *
     * @return (boolean) True if the customer has a membership card.
     */
    public boolean hasMembership() {
        boolean hasCard = (this.membershipCard != null);
        return hasCard;
    }

    /**
     * Adds earned membership points to the customer's card.
     * Safe to call even if no card is assigned.
     *
     * @param amount (double) The amount spent in the transaction.
     */
    public void earnPoints(double amount) {
        if (this.membershipCard != null) {
            this.membershipCard.accumulatePoints(amount);
        }
    }

    /**
    * Refunds redeemed points if a transaction is cancelled.
    *
    * @param refundedPoints (int) The number of points to refund.
    */
    public void refundPoints(int refundedPoints) {
        if (this.membershipCard != null && refundedPoints > 0) {
            this.membershipCard.refundPoints(refundedPoints);
        }
    }

    /**
     * Returns a formatted string representation of the customer
     * for console display or receipts.
     *
     * @return (String) A descriptive summary of the customer's information.
     */
    @Override
    public String toString() {
        String summary;
        summary = "Customer ID: " + this.customerID + " | Name: " + this.name;

        if (this.isSenior) {
            summary = summary + " (Senior/PWD)";
        }

        if (this.membershipCard != null) {
            summary = summary + " | Points: " + this.membershipCard.getPoints();
        } else {
            summary = summary + " (No Membership)";
        }

        return summary;
    }
}

