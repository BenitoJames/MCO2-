package model;

import java.time.LocalDate;

/**
 * Represents a user/customer in the convenience store.
 * Updated to support user authentication and membership cards.
 */
public class Customer {

    private String userID;                  // Unique user ID (DLSUser-XXX)
    private String lastName;                // Customer last name
    private String firstName;               // Customer first name
    private String middleName;              // Customer middle name (optional)
    private String password;                // User password
    private String membershipCardID;        // Membership card ID (DLSUCS-XXXXXXXX)
    private LocalDate cardExpiryDate;       // Membership card expiry
    private int points;                     // Membership points
    private boolean isGuest;                // True if guest user

    /**
     * Constructs a Customer object with user ID and name.
     *
     * @param userID     (String) The user's unique ID.
     * @param lastName   (String) The user's last name.
     * @param firstName  (String) The user's first name.
     * @param middleName (String) The user's middle name (can be null/empty).
     * @param password (String) The user's password.
     */
    public Customer(String userID, String lastName, String firstName, String middleName, String password) {
        this.userID = userID;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName != null ? middleName : "";
        this.password = password;
        this.membershipCardID = null;
        this.cardExpiryDate = null;
        this.points = 0;
        this.isGuest = false;
    }
    
    /**
     * Constructs a guest Customer object.
     */
    public Customer() {
        this.userID = "GUEST";
        this.lastName = "Guest";
        this.firstName = "User";
        this.middleName = "";
        this.password = "";
        this.membershipCardID = null;
        this.cardExpiryDate = null;
        this.points = 0;
        this.isGuest = true;
    }
    
    /**
     * Constructs a Customer object from ID and name (backward compatibility).
     * This is for old console code that doesn't use authentication.
     *
     * @param userID (String) The user's ID.
     * @param name (String) The user's full name.
     */
    public Customer(String userID, String name) {
        this.userID = userID;
        // Parse name - assume "LastName FirstName" or just "Name"
        String[] nameParts = name.trim().split("\\s+", 2);
        if (nameParts.length >= 2) {
            this.lastName = nameParts[0];
            this.firstName = nameParts[1];
        } else {
            this.lastName = name;
            this.firstName = "";
        }
        this.middleName = "";
        this.password = "";
        this.membershipCardID = null;
        this.cardExpiryDate = null;
        this.points = 0;
        this.isGuest = false;
    }


    /**
     * Returns the user's ID.
     *
     * @return (String) The user's unique identifier.
     */
    public String getUserID() {
        return userID;
    }
    
    /**
     * Returns the user's ID (alias for getUserID for backward compatibility).
     *
     * @return (String) The user's unique identifier.
     */
    public String getCustomerID() {
        return userID;
    }

    /**
     * Returns the user's full name.
     *
     * @return (String) The user's full name.
     */
    public String getName() {
        String fullName = lastName + ", " + firstName;
        if (middleName != null && !middleName.isEmpty()) {
            fullName += " " + middleName;
        }
        return fullName;
    }
    
    /**
     * Returns the user's last name.
     *
     * @return (String) The user's last name.
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * Returns the user's first name.
     *
     * @return (String) The user's first name.
     */
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * Returns the user's middle name.
     *
     * @return (String) The user's middle name.
     */
    public String getMiddleName() {
        return middleName;
    }
    
    /**
     * Returns the user's password.
     *
     * @return (String) The user's password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Updates the user's name.
     *
     * @param lastName   (String) The new last name.
     * @param firstName  (String) The new first name.
     * @param middleName (String) The new middle name.
     */
    public void setName(String lastName, String firstName, String middleName) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName != null ? middleName : "";
    }
    
    /**
     * Updates the user's password.
     *
     * @param password (String) The new password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the membership card ID.
     *
     * @return (String) The membership card ID, or null if none.
     */
    public String getMembershipCardID() {
        return membershipCardID;
    }
    
    /**
     * Returns the membership card (for backward compatibility).
     *
     * @return (MembershipCard) A virtual membership card object, or null if none.
     */
    public MembershipCard getMembershipCard() {
        if (membershipCardID == null) {
            return null;
        }
        // Create a virtual MembershipCard wrapper
        return new MembershipCard(points);
    }
    
    /**
     * Returns the card expiry date.
     *
     * @return (LocalDate) The card expiry date, or null if none.
     */
    public LocalDate getCardExpiryDate() {
        return cardExpiryDate;
    }
    
    /**
     * Returns the user's points.
     *
     * @return (int) The current points balance.
     */
    public int getPoints() {
        return points;
    }
    
    /**
     * Checks if the user is a guest.
     *
     * @return (boolean) True if guest user.
     */
    public boolean isGuest() {
        return isGuest;
    }

    /**
     * Returns whether the customer is eligible for senior/PWD discount.
     * This is now per-transaction only, not stored.
     *
     * @return (boolean) Always false, since senior/PWD is per-transaction.
     */
    public boolean getIsSenior() {
        return false;
    }

    /**
     * Assigns a membership card to the user.
     *
     * @param cardID     (String) The membership card ID.
     * @param expiryDate (LocalDate) The card expiry date.
     */
    public void assignMembershipCard(String cardID, LocalDate expiryDate) {
        this.membershipCardID = cardID;
        this.cardExpiryDate = expiryDate;
    }
    
    /**
     * Assigns a membership card to the user (backward compatibility).
     *
     * @param card (MembershipCard) The membership card to assign.
     */
    public void assignMembershipCard(MembershipCard card) {
        // For backward compatibility, but we now store card details directly
        if (card != null) {
            this.points = card.getPoints();
        }
    }

    /**
     * Sets the senior/PWD status for the customer.
     * Note: This is now per-transaction only, not stored.
     *
     * @param seniorStatus (boolean) Ignored, kept for backward compatibility.
     */
    public void setIsSenior(boolean seniorStatus) {
        // No longer stored, kept for backward compatibility
    }

    /**
     * Determines if the customer has a valid membership card.
     *
     * @return (boolean) True if the customer has a valid membership card.
     */
    public boolean hasMembership() {
        if (membershipCardID == null || cardExpiryDate == null) {
            return false;
        }
        // Check if card is still valid
        return !cardExpiryDate.isBefore(LocalDate.now());
    }
    
    /**
     * Adds points to the user's account.
     *
     * @param amount (double) The total amount spent.
     */
    public void earnPoints(double amount) {
        if (hasMembership()) {
            int earnedPoints = (int) Math.floor(amount / 50.0);
            if (earnedPoints > 0) {
                this.points += earnedPoints;
            }
        }
    }
    
    /**
     * Uses points for a discount.
     *
     * @param pointsToUse (int) The number of points to use.
     * @return (double) The discount amount (1 point = 1 peso).
     */
    public double usePoints(int pointsToUse) {
        if (pointsToUse <= 0) {
            return 0;
        }
        int actualPoints = Math.min(pointsToUse, this.points);
        this.points -= actualPoints;
        return actualPoints;
    }
    
    /**
     * Refunds points back to the user (for cancelled transactions).
     *
     * @param refundedPoints (int) The number of points to refund.
     */
    public void refundPoints(int refundedPoints) {
        if (refundedPoints > 0) {
            this.points += refundedPoints;
        }
    }
    
    /**
     * Sets the user's points (for staff editing).
     *
     * @param points (int) The new points balance.
     */
    public void setPoints(int points) {
        this.points = Math.max(0, points);
    }

    /**
     * Returns a summary string for displaying customer details.
     *
     * @return (String) A formatted string.
     */
    public String getSummary() {
        String summary = "User ID: " + this.userID + " | Name: " + getName();
        
        if (hasMembership()) {
            summary += " | Card: " + membershipCardID + " | Points: " + points;
        }
        
        return summary;
    }

    /**
     * Returns a formatted string representation of the customer
     * for console display or receipts.
     *
     * @return (String) A descriptive summary of the customer's information.
     */
    @Override
    public String toString() {
        return getSummary();
    }
}

