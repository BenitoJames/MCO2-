package model;

/**
 * Represents a customer's membership card, 
 * used for accumulating and redeeming points.
 */
public class MembershipCard {
    
    private int points; // The total reward points

    /**
     * Constructs a MembershipCard object.
     *
     * @param initialPoints The starting number of points.
     */
    public MembershipCard(int initialPoints) {
        if (initialPoints >= 0) {
            this.points = initialPoints;
        } else {
            this.points = 0;
        }
    }

    /**
     * Gets the current number of points.
     *
     * @return The current point balance.
     */
    public int getPoints() {
        return this.points;
    }

    /**
     * Adds points to the card based on the amount spent.
     * Rule: 1 point for every P50 spent.
     *
     * @param amount The total amount spent.
     * @return The number of points earned.
     */
    public int accumulatePoints(double amount) {
        // 1 point per P50
        int earnedPoints = (int) Math.floor(amount / 50.0);
        if (earnedPoints > 0) {
            this.points += earnedPoints;
        }
        return earnedPoints;
    }

    /**
     * Redeems a specified number of points as a discount.
     * 1 point = P1.00 discount.
     *
     * @param pointsToUse The number of points to redeem.
     * @return The discount value (equal to points used).
     */
    public double usePoints(int pointsToUse) {
        int pointsUsed = 0;
        
        if (pointsToUse <= 0) {
            pointsUsed = 0;
        } else if (pointsToUse > this.points) {
            // Use all available points if requesting too many
            pointsUsed = this.points;
        } else {
            // Use the requested amount
            pointsUsed = pointsToUse;
        }
        
        this.points -= pointsUsed;
        return pointsUsed; // 1 point = 1 peso discount
    }
    
    /**
     * NEW METHOD: Refunds points back to the card.
     * Used for cancelled transactions.
     *
     * @param pointsToRefund The number of points to add back.
     */
    public void refundPoints(int pointsToRefund) {
        if (pointsToRefund > 0) {
            this.points += pointsToRefund;
        }
    }

    /**
     * Returns a string summary of the card.
     *
     * @return A formatted string.
     */
    @Override
    public String toString() {
        return "Membership Card (" + this.points + " points)";
    }
}

