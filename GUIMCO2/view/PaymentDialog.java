package view;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import model.*;
import util.StoreDataHandler;

/**
 * Dialog for handling payment during checkout.
 * Flow: Senior/PWD → Membership → Order Summary → Payment
 */
public class PaymentDialog extends JDialog {
    private final Transaction transaction;
    private Customer customer;
    private final StoreDataHandler dataHandler;
    private final List<Customer> customerList;
    private boolean transactionCompleted = false;
    
    private boolean isSenior = false;
    private double membershipFee = 0.0;
    private double subtotal = 0.0;
    private double vat = 0.0;
    private double seniorDiscount = 0.0;
    private double totalDue = 0.0;
    private int pointsToUse = 0;
    
    /**
     * Constructs a payment dialog and runs the checkout flow.
     */
    public PaymentDialog(Window parent, Transaction transaction, Customer customer,
                        StoreDataHandler dataHandler, List<Customer> customerList) {
        super(parent, "Checkout", ModalityType.APPLICATION_MODAL);
        this.transaction = transaction;
        this.customer = customer;
        this.dataHandler = dataHandler;
        this.customerList = customerList;
        
        // Run checkout flow immediately
        if (runCheckoutFlow()) {
            transactionCompleted = true;
        }
    }
    
    /**
     * Runs the complete checkout flow.
     * @return true if checkout completed, false if cancelled
     */
    private boolean runCheckoutFlow() {
        // Step 1: Senior/PWD Dialog (for EVERYONE)
        if (!handleSeniorPWDDialog()) {
            return false; // User cancelled
        }
        
        // Step 2: Membership Card Dialog
        if (!handleMembershipDialog()) {
            return false; // User cancelled
        }
        
        // Step 3: Calculate Order Summary
        calculateOrderSummary();
        
        // Step 4: Show Order Summary
        if (!showOrderSummary()) {
            return false; // User cancelled
        }
        
        // Step 5: Payment Details
        return handlePayment();
    }
    
    /**
     * Step 1: Senior/PWD Dialog (shown to EVERYONE).
     */
    private boolean handleSeniorPWDDialog() {
        int result = JOptionPane.showConfirmDialog(
            null,
            "Are you a Senior Citizen or PWD?",
            "Senior Citizen / PWD Discount",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Ask for Senior/PWD ID
            String seniorID = JOptionPane.showInputDialog(
                null,
                "Enter your Senior/PWD ID\n(format: SRC-XXXX or PWD-XXXX):",
                "Senior/PWD ID Validation",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (seniorID == null) {
                return false; // User cancelled
            }
            
            seniorID = seniorID.trim().toUpperCase();
            
            // Validate format
            if (Pattern.matches("^(SRC|PWD)-\\d{4}$", seniorID)) {
                isSenior = true;
                JOptionPane.showMessageDialog(
                    null,
                    "Senior/PWD discount applied!\n(20% off after VAT deduction)",
                    "Discount Applied",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    "Invalid Senior/PWD ID format!\nDiscount not applied.",
                    "Invalid Format",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
        
        return true;
    }
    
    /**
     * Step 2: Membership Card Dialog.
     */
    private boolean handleMembershipDialog() {
        // Skip if user already has membership
        if (customer.hasMembership()) {
            return true;
        }
        
        if (customer.isGuest()) {
            // Guest user - show 3 options
            String[] options = {"Yes, purchase for ₱50.00", "No, continue without membership", "I have a card already"};
            int choice = JOptionPane.showOptionDialog(
                null,
                "Would you like to purchase a Membership Card?\n\n" +
                "Benefits:\n" +
                "• Earn 1 point for every ₱50 spent\n" +
                "• Redeem points for discounts\n" +
                "• Valid for 1 year\n\n" +
                "Cost: ₱50.00",
                "Membership Card",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );
            
            if (choice == 0) {
                // Purchase membership - create account for guest
                return handleGuestMembershipPurchase();
            } else if (choice == 2) {
                // Guest has existing card
                return handleExistingMembershipCard();
            }
            // else choice == 1 or CLOSED_OPTION: continue without membership
            
        } else {
            // Signed-in user without membership
            int result = JOptionPane.showConfirmDialog(
                null,
                "Would you like to purchase a Membership Card for ₱50.00?\n\n" +
                "Benefits:\n" +
                "• Earn 1 point for every ₱50 spent\n" +
                "• Redeem points for discounts\n" +
                "• Valid for 1 year",
                "Membership Card Purchase",
                JOptionPane.YES_NO_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                membershipFee = 50.0;
                String membershipID = generateMembershipID();
                LocalDate expiryDate = LocalDate.now().plusYears(1);
                customer.assignMembershipCard(membershipID, expiryDate);
                
                JOptionPane.showMessageDialog(
                    null,
                    "Membership card purchased!\n\n" +
                    "Membership ID: " + membershipID + "\n" +
                    "Valid until: " + expiryDate,
                    "Membership Card Purchased",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
        
        return true;
    }
    
    /**
     * Handles guest purchasing membership (creates account).
     */
    private boolean handleGuestMembershipPurchase() {
        // Get name details
        JTextField lastNameField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField middleNameField = new JTextField();
        
        JPanel namePanel = new JPanel(new GridLayout(3, 2, 5, 5));
        namePanel.add(new JLabel("Last Name:"));
        namePanel.add(lastNameField);
        namePanel.add(new JLabel("First Name:"));
        namePanel.add(firstNameField);
        namePanel.add(new JLabel("Middle Name (optional):"));
        namePanel.add(middleNameField);
        
        int result = JOptionPane.showConfirmDialog(
            null,
            namePanel,
            "Create Account",
            JOptionPane.OK_CANCEL_OPTION
        );
        
        if (result != JOptionPane.OK_OPTION) {
            return false;
        }
        
        String lastName = lastNameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String middleName = middleNameField.getText().trim();
        
        if (lastName.isEmpty() || firstName.isEmpty()) {
            JOptionPane.showMessageDialog(
                null,
                "Last Name and First Name are required!",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        // Generate credentials
        membershipFee = 50.0;
        String userID = generateUserID();
        String password = "DLSUser2025";
        String membershipID = generateMembershipID();
        LocalDate expiryDate = LocalDate.now().plusYears(1);
        
        // Create new customer account
        customer = new Customer(userID, lastName, firstName, middleName, password);
        customer.assignMembershipCard(membershipID, expiryDate);
        customerList.add(customer);
        
        // Show account details
        JOptionPane.showMessageDialog(
            null,
            "=== Account Created ===\n\n" +
            "User ID: " + userID + "\n" +
            "Password: " + password + "\n" +
            "Membership ID: " + membershipID + "\n" +
            "Valid until: " + expiryDate + "\n\n" +
            "An account has been created for you.\n" +
            "You can sign in next time!",
            "Account Created",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        return true;
    }
    
    /**
     * Handles guest using existing membership card.
     */
    private boolean handleExistingMembershipCard() {
        String membershipID = JOptionPane.showInputDialog(
            null,
            "Enter your Membership Card ID\n(format: DLSUCS-XXXXXXXX):",
            "Existing Membership Card",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (membershipID == null) {
            return false;
        }
        
        membershipID = membershipID.trim();
        
        // Find customer with this membership ID
        Customer foundCustomer = null;
        for (Customer c : customerList) {
            if (c.getMembershipCardID() != null && c.getMembershipCardID().equals(membershipID)) {
                foundCustomer = c;
                break;
            }
        }
        
        if (foundCustomer != null) {
            customer = foundCustomer;
            JOptionPane.showMessageDialog(
                null,
                "Welcome back, " + customer.getName() + "!\n" +
                "Current points: " + customer.getPoints(),
                "Membership Card Found",
                JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                null,
                "Membership card not found in system.\nContinuing as guest.",
                "Card Not Found",
                JOptionPane.WARNING_MESSAGE
            );
        }
        
        return true;
    }
    
    /**
     * Step 3: Calculate Order Summary.
     */
    private void calculateOrderSummary() {
        // Use transaction's calculate method with senior/PWD flag
        transaction.calculateTotals(isSenior);
        
        // Get subtotal from transaction
        subtotal = transaction.getSubtotal();
        
        // Calculate VAT (12%)
        vat = subtotal * 0.12;
        
        // Calculate Senior/PWD discount (if applicable)
        seniorDiscount = 0.0;
        if (isSenior) {
            double amountAfterVAT = subtotal - vat;
            seniorDiscount = amountAfterVAT * 0.20;
        }
        
        // Calculate total due
        totalDue = subtotal - seniorDiscount + membershipFee;
    }
    
    /**
     * Step 4: Show Order Summary with points redemption option.
     */
    private boolean showOrderSummary() {
        // Build summary text
        StringBuilder summary = new StringBuilder();
        summary.append("========== ORDER SUMMARY ==========\n\n");
        summary.append(String.format("Subtotal:                ₱%.2f\n", subtotal));
        summary.append(String.format("VAT (12%%):               ₱%.2f\n", vat));
        
        if (isSenior) {
            summary.append(String.format("Senior/PWD Discount:    -₱%.2f\n", seniorDiscount));
        }
        
        if (membershipFee > 0) {
            summary.append(String.format("Membership Card:        +₱%.2f\n", membershipFee));
        }
        
        summary.append("-----------------------------------\n");
        summary.append(String.format("TOTAL DUE:               ₱%.2f\n", totalDue));
        summary.append("===================================\n\n");
        
        // Add points information if member
        if (customer.hasMembership()) {
            int currentPoints = customer.getPoints();
            int pointsToEarn = (int) ((subtotal - seniorDiscount) / 50);
            
            summary.append(String.format("Current Points: %d\n", currentPoints));
            summary.append(String.format("Points to be earned: %d (1 point per ₱50)\n\n", pointsToEarn));
            
            // Point redemption
            if (currentPoints > 0) {
                int result = JOptionPane.showConfirmDialog(
                    null,
                    summary.toString() + "Would you like to use points to pay?\n(1 point = ₱1)",
                    "Order Summary",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    int maxPoints = (int) Math.min(currentPoints, totalDue);
                    String input = JOptionPane.showInputDialog(
                        null,
                        "Enter points to use (max " + maxPoints + "):",
                        "Point Redemption",
                        JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (input != null) {
                        try {
                            pointsToUse = Integer.parseInt(input.trim());
                            
                            if (pointsToUse > 0 && pointsToUse <= maxPoints) {
                                totalDue -= pointsToUse;
                                customer.usePoints(pointsToUse);
                                
                                JOptionPane.showMessageDialog(
                                    null,
                                    "Redeemed " + pointsToUse + " points.\n" +
                                    String.format("New Total Due: ₱%.2f", totalDue),
                                    "Points Redeemed",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                            }
                        } catch (NumberFormatException e) {
                            // Invalid input, continue without redemption
                        }
                    }
                }
            } else {
                // Just show summary without redemption option
                JOptionPane.showMessageDialog(
                    null,
                    summary.toString(),
                    "Order Summary",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        } else {
            // Not a member, just show summary
            JOptionPane.showMessageDialog(
                null,
                summary.toString(),
                "Order Summary",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
        
        return true;
    }
    
    /**
     * Step 5: Handle Payment.
     */
    private boolean handlePayment() {
        // Payment method selection
        String[] paymentMethods = {"Cash", "Card"};
        JPanel paymentPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        paymentPanel.add(new JLabel(String.format("Total Due: ₱%.2f", totalDue)));
        paymentPanel.add(new JLabel("Select Payment Method:"));
        JComboBox<String> methodCombo = new JComboBox<>(paymentMethods);
        paymentPanel.add(methodCombo);
        
        int result = JOptionPane.showConfirmDialog(
            null,
            paymentPanel,
            "Payment Method",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result != JOptionPane.OK_OPTION) {
            return false; // User cancelled
        }
        
        String paymentMethod = (String) methodCombo.getSelectedItem();
        double amountPaid = 0.0;
        double change = 0.0;
        String cardDetails = "";
        
        if ("Cash".equals(paymentMethod)) {
            // Cash payment
            if (!handleCashPayment()) {
                return false;
            }
            amountPaid = this.amountPaid;
            change = this.change;
        } else {
            // Card payment
            cardDetails = handleCardPayment();
            if (cardDetails == null) {
                return false; // User cancelled or validation failed
            }
            amountPaid = totalDue;
            change = 0.0;
        }
        
        // Calculate points to earn
        int pointsEarned = 0;
        if (customer.hasMembership()) {
            pointsEarned = (int) ((subtotal - seniorDiscount) / 50);
            customer.earnPoints(pointsEarned);
        }
        
        // Save customer data
        dataHandler.saveCustomers(customerList);
        
        // Show receipt
        showReceipt(paymentMethod, amountPaid, change, pointsEarned, cardDetails);
        
        return true;
    }
    
    // Fields for cash payment
    private double amountPaid = 0.0;
    private double change = 0.0;
    
    /**
     * Handles cash payment.
     */
    private boolean handleCashPayment() {
        while (true) {
            String input = JOptionPane.showInputDialog(
                null,
                String.format("Total Due: ₱%.2f\n\nEnter amount to pay:", totalDue),
                "Cash Payment",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (input == null) {
                return false; // User cancelled
            }
            
            try {
                double amount = Double.parseDouble(input.trim());
                
                if (amount < totalDue) {
                    JOptionPane.showMessageDialog(
                        null,
                        String.format("Insufficient payment!\nTotal due: ₱%.2f\nYou entered: ₱%.2f", totalDue, amount),
                        "Insufficient Payment",
                        JOptionPane.ERROR_MESSAGE
                    );
                    continue;
                }
                
                this.amountPaid = amount;
                this.change = amount - totalDue;
                
                // Show change confirmation
                JOptionPane.showMessageDialog(
                    null,
                    String.format("Payment Received: ₱%.2f\nChange: ₱%.2f", amount, this.change),
                    "Payment Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                return true;
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Please enter a valid amount!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Handles card payment with validation.
     * @return Card details string, or null if cancelled/failed
     */
    private String handleCardPayment() {
        JTextField cardNumberField = new JTextField(16);
        JTextField cvvField = new JTextField(3);
        JTextField expiryField = new JTextField(5);
        
        JPanel cardPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        cardPanel.add(new JLabel("Card Number:"));
        cardPanel.add(cardNumberField);
        cardPanel.add(new JLabel("CVV:"));
        cardPanel.add(cvvField);
        cardPanel.add(new JLabel("Expiry (MM/YY):"));
        cardPanel.add(expiryField);
        cardPanel.add(new JLabel(""));
        cardPanel.add(new JLabel("(Visa or Mastercard only)"));
        
        int result = JOptionPane.showConfirmDialog(
            null,
            cardPanel,
            "Card Payment",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        
        String cardNumber = cardNumberField.getText().trim();
        String cvv = cvvField.getText().trim();
        String expiry = expiryField.getText().trim();
        
        // Validate card number (16 digits, Visa or Mastercard)
        if (!cardNumber.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(
                null,
                "Invalid card number!\nMust be 16 digits.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return handleCardPayment(); // Retry
        }
        
        // Check if Visa (starts with 4) or Mastercard (starts with 51-55)
        if (!cardNumber.startsWith("4") && 
            !(cardNumber.startsWith("51") || cardNumber.startsWith("52") || 
              cardNumber.startsWith("53") || cardNumber.startsWith("54") || 
              cardNumber.startsWith("55"))) {
            JOptionPane.showMessageDialog(
                null,
                "Card not accepted!\nOnly Visa or Mastercard.",
                "Invalid Card",
                JOptionPane.ERROR_MESSAGE
            );
            return handleCardPayment(); // Retry
        }
        
        // Validate CVV (3 digits)
        if (!cvv.matches("\\d{3}")) {
            JOptionPane.showMessageDialog(
                null,
                "Invalid CVV!\nMust be 3 digits.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return handleCardPayment(); // Retry
        }
        
        // Validate expiry (MM/YY format and future date)
        if (!expiry.matches("\\d{2}/\\d{2}")) {
            JOptionPane.showMessageDialog(
                null,
                "Invalid expiry date!\nFormat: MM/YY",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return handleCardPayment(); // Retry
        }
        
        String[] parts = expiry.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = 2000 + Integer.parseInt(parts[1]);
        
        if (month < 1 || month > 12) {
            JOptionPane.showMessageDialog(
                null,
                "Invalid month!\nMust be 01-12.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return handleCardPayment(); // Retry
        }
        
        // Check if card is expired
        java.time.YearMonth cardExpiry = java.time.YearMonth.of(year, month);
        java.time.YearMonth now = java.time.YearMonth.now();
        if (cardExpiry.isBefore(now)) {
            JOptionPane.showMessageDialog(
                null,
                "Card expired!\nExpiry date: " + expiry,
                "Expired Card",
                JOptionPane.ERROR_MESSAGE
            );
            return handleCardPayment(); // Retry
        }
        
        // Show confirmation dialog with card details
        String cardType = cardNumber.startsWith("4") ? "Visa" : "Mastercard";
        String maskedCard = "**** **** **** " + cardNumber.substring(12);
        
        String confirmMessage = String.format(
            "Card Type: %s\n" +
            "Card Number: %s\n" +
            "Expiry: %s\n\n" +
            "Amount to charge: ₱%.2f\n\n" +
            "Proceed with payment?",
            cardType, maskedCard, expiry, totalDue
        );
        
        int confirm = JOptionPane.showConfirmDialog(
            null,
            confirmMessage,
            "Confirm Card Payment",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return null; // User cancelled
        }
        
        return String.format("%s %s", cardType, maskedCard);
    }
    
    /**
     * Shows the receipt.
     */
    private void showReceipt(String paymentMethod, double amountPaid, double change, 
                             int pointsEarned, String cardDetails) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("╔════════════════════════════════════════╗\n");
        receipt.append("║     DLSU CONVENIENCE STORE RECEIPT    ║\n");
        receipt.append("╚════════════════════════════════════════╝\n\n");
        
        // Date and time
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        receipt.append("Date: ").append(now.format(formatter)).append("\n");
        receipt.append("Customer: ").append(customer.getName()).append("\n");
        if (customer.hasMembership()) {
            receipt.append("Member ID: ").append(customer.getMembershipCardID()).append("\n");
        }
        receipt.append("\n");
        receipt.append("----------------------------------------\n");
        receipt.append("ITEMS PURCHASED\n");
        receipt.append("----------------------------------------\n");
        
        // Print all items from transaction
        for (CartItem item : transaction.getCartItems()) {
            receipt.append(String.format("%-20s x%d  ₱%.2f\n", 
                item.getProduct().getName(), 
                item.getQuantity(),
                item.getProduct().getPrice() * item.getQuantity()));
        }
        
        receipt.append("----------------------------------------\n");
        receipt.append(String.format("Subtotal:                ₱%.2f\n", subtotal));
        receipt.append(String.format("VAT (12%%):               ₱%.2f\n", vat));
        
        if (isSenior) {
            receipt.append(String.format("Senior/PWD Discount:    -₱%.2f\n", seniorDiscount));
        }
        
        if (membershipFee > 0) {
            receipt.append(String.format("Membership Card:        +₱%.2f\n", membershipFee));
        }
        
        if (pointsToUse > 0) {
            receipt.append(String.format("Points Redeemed:        -₱%d.00\n", pointsToUse));
        }
        
        receipt.append("----------------------------------------\n");
        receipt.append(String.format("TOTAL DUE:               ₱%.2f\n", totalDue));
        receipt.append("----------------------------------------\n\n");
        
        receipt.append("PAYMENT DETAILS\n");
        receipt.append("Payment Method: ").append(paymentMethod).append("\n");
        
        if ("Cash".equals(paymentMethod)) {
            receipt.append(String.format("Amount Paid:             ₱%.2f\n", amountPaid));
            receipt.append(String.format("Change:                  ₱%.2f\n", change));
        } else {
            receipt.append("Card: ").append(cardDetails).append("\n");
            receipt.append(String.format("Amount Charged:          ₱%.2f\n", amountPaid));
        }
        
        if (customer.hasMembership()) {
            receipt.append("\n----------------------------------------\n");
            receipt.append("MEMBERSHIP POINTS\n");
            receipt.append("----------------------------------------\n");
            receipt.append(String.format("Points Earned: +%d\n", pointsEarned));
            receipt.append(String.format("Total Points: %d\n", customer.getPoints()));
        }
        
        receipt.append("\n");
        receipt.append("========================================\n");
        receipt.append("   Thank you for shopping with us!\n");
        receipt.append("       Please come again soon!\n");
        receipt.append("========================================\n");
        
        // Show receipt in scrollable dialog
        JTextArea receiptArea = new JTextArea(receipt.toString());
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setPreferredSize(new Dimension(500, 600));
        
        JOptionPane.showMessageDialog(
            null,
            scrollPane,
            "Transaction Receipt",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // Close the checkout dialog after showing receipt
        dispose();
    }
    
    /**
     * Generates a new user ID in the format DLSUser-XXX.
     */
    private String generateUserID() {
        int maxNumber = 0;
        for (Customer c : customerList) {
            String id = c.getUserID();
            if (id.startsWith("DLSUser-")) {
                try {
                    int number = Integer.parseInt(id.substring(8));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }
        return String.format("DLSUser-%03d", maxNumber + 1);
    }
    
    /**
     * Generates a membership card ID in the format DLSUCS-XXXXXXXX.
     */
    private String generateMembershipID() {
        return "DLSUCS-" + String.format("%08d", (int) (Math.random() * 100000000));
    }
    
    /**
     * Returns whether the transaction was completed.
     */
    public boolean isTransactionCompleted() {
        return transactionCompleted;
    }
    
    /**
     * Gets the updated customer (important for guests who created accounts).
     */
    public Customer getCustomer() {
        return customer;
    }
}
