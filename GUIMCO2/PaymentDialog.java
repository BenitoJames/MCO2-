package view;

import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import model.*;
import util.StoreDataHandler;

/**
 * Dialog for handling payment during checkout.
 * Supports membership card purchase, Senior/PWD discounts, and points redemption.
 */
public class PaymentDialog extends JDialog {
    private final Transaction transaction;
    private final Customer customer;
    private final StoreDataHandler dataHandler;
    private final List<Customer> customerList;
    private boolean transactionCompleted = false;
    
    private JLabel totalLabel;
    private JLabel currentPointsLabel;
    private JLabel pointsToEarnLabel;
    private JTextField pointsField;
    private JComboBox<String> paymentMethodCombo;
    private JTextField amountField;
    private JCheckBox seniorPWDCheckbox;
    private JTextField seniorPWDCardField;
    private JButton membershipBtn;
    
    private boolean seniorPWDApplied = false;
    private double membershipCardCost = 0.0;
    
    /**
     * Constructs a payment dialog.
     *
     * @param parent The parent window
     * @param transaction The transaction to process
     * @param customer The customer making the purchase
     * @param dataHandler The data handler for saving
     * @param customerList The list of all customers
     */
    public PaymentDialog(Window parent, Transaction transaction, Customer customer,
                        StoreDataHandler dataHandler, List<Customer> customerList) {
        super(parent, "Checkout", ModalityType.APPLICATION_MODAL);
        this.transaction = transaction;
        this.customer = customer;
        this.dataHandler = dataHandler;
        this.customerList = customerList;
        
        setupUI();
        setSize(600, 750);
        setLocationRelativeTo(parent);
    }
    
    /**
     * Sets up the dialog UI.
     */
    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(34, 139, 34));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Checkout");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Center panel with all checkout info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Membership card purchase option (if signed in without card)
        if (!customer.isGuest() && !customer.hasMembership()) {
            centerPanel.add(createMembershipPurchasePanel());
            centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }
        
        // Senior/PWD discount option
        centerPanel.add(createSeniorPWDPanel());
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Transaction summary
        centerPanel.add(createSummaryPanel());
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Points info (if member)
        if (customer.hasMembership()) {
            centerPanel.add(createPointsInfoPanel());
            centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }
        
        // Payment details
        centerPanel.add(createPaymentPanel());
        
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Creates the membership card purchase panel.
     */
    private JPanel createMembershipPurchasePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Membership Card"));
        panel.setBackground(new Color(255, 255, 220));
        
        JLabel infoLabel = new JLabel("<html>You don't have a membership card yet!<br>" +
            "Purchase one now to earn points and get exclusive benefits.</html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(infoLabel, BorderLayout.CENTER);
        
        membershipBtn = new JButton("Purchase Card (₱50.00)");
        membershipBtn.setBackground(new Color(70, 130, 180));
        membershipBtn.setForeground(Color.WHITE);
        membershipBtn.setFocusPainted(false);
        membershipBtn.addActionListener(e -> handleMembershipPurchase());
        panel.add(membershipBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Creates the Senior/PWD discount panel.
     */
    private JPanel createSeniorPWDPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Senior Citizen / PWD Discount"));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        seniorPWDCheckbox = new JCheckBox("I am a Senior Citizen or PWD");
        seniorPWDCheckbox.addActionListener(e -> toggleSeniorPWDFields());
        topPanel.add(seniorPWDCheckbox);
        panel.add(topPanel, BorderLayout.NORTH);
        
        JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cardPanel.add(new JLabel("Card ID:"));
        seniorPWDCardField = new JTextField(15);
        seniorPWDCardField.setEnabled(false);
        cardPanel.add(seniorPWDCardField);
        
        JButton applyBtn = new JButton("Apply Discount");
        applyBtn.setEnabled(false);
        applyBtn.addActionListener(e -> applySeniorPWDDiscount());
        cardPanel.add(applyBtn);
        
        seniorPWDCheckbox.addActionListener(e -> applyBtn.setEnabled(seniorPWDCheckbox.isSelected()));
        
        panel.add(cardPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void toggleSeniorPWDFields() {
        boolean enabled = seniorPWDCheckbox.isSelected();
        seniorPWDCardField.setEnabled(enabled);
        if (!enabled) {
            seniorPWDCardField.setText("");
            // Revert discount if unchecked
            if (seniorPWDApplied) {
                seniorPWDApplied = false;
                recalculateTotal();
            }
        }
    }
    
    private void applySeniorPWDDiscount() {
        if (seniorPWDApplied) {
            JOptionPane.showMessageDialog(this,
                "Senior/PWD discount already applied!",
                "Already Applied",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String cardID = seniorPWDCardField.getText().trim().toUpperCase();
        
        if (cardID.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter your Senior/PWD Card ID.",
                "Missing Card ID",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate format: SRC-XXXX or PWD-XXXX
        if (!Pattern.matches("^(SRC|PWD)-\\d{4}$", cardID)) {
            JOptionPane.showMessageDialog(this,
                "Invalid Card ID format!\nMust be SRC-#### or PWD-#### (e.g., SRC-1234)",
                "Invalid Format",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        seniorPWDApplied = true;
        transaction.setSeniorPWDCardID(cardID);
        recalculateTotal();
        
        JOptionPane.showMessageDialog(this,
            "Senior/PWD discount applied!\nVAT removed and 20% discount applied.",
            "Discount Applied",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void recalculateTotal() {
        // Recalculate transaction with Senior/PWD discount
        transaction.calculateTotals(seniorPWDApplied);
        updateSummaryDisplay();
        updatePointsDisplay();
    }
    
    /**
     * Creates the transaction summary panel.
     */
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Order Summary"));
        
        JTextArea summaryArea = new JTextArea(transaction.getTotalsString());
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryArea.setBackground(new Color(250, 250, 250));
        panel.add(summaryArea);
        
        totalLabel = new JLabel("TOTAL DUE: ₱" + String.format("%.2f", transaction.getFinalTotal()));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(34, 139, 34));
        panel.add(totalLabel);
        
        return panel;
    }
    
    /**
     * Creates the points information panel for members.
     */
    private JPanel createPointsInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Membership Points"));
        
        // Current points
        JPanel currentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        currentPointsLabel = new JLabel("Current Points: " + customer.getPoints());
        currentPointsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        currentPanel.add(currentPointsLabel);
        panel.add(currentPanel);
        
        // Points to earn
        int pointsToEarn = (int) Math.floor(transaction.getFinalTotal() / 50.0);
        JPanel earnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pointsToEarnLabel = new JLabel("Points to Earn: " + pointsToEarn + 
            " (₱" + String.format("%.2f", transaction.getFinalTotal()) + " ÷ 50)");
        pointsToEarnLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        pointsToEarnLabel.setForeground(new Color(0, 100, 0));
        earnPanel.add(pointsToEarnLabel);
        panel.add(earnPanel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Points redemption
        JPanel redeemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        redeemPanel.add(new JLabel("Use Points (1 point = ₱1):"));
        pointsField = new JTextField(8);
        pointsField.setText("0");
        redeemPanel.add(pointsField);
        
        JButton applyBtn = new JButton("Apply");
        applyBtn.addActionListener(e -> applyPoints());
        redeemPanel.add(applyBtn);
        
        panel.add(redeemPanel);
        
        return panel;
    }
    
    private void handleMembershipPurchase() {
        MembershipCardPurchaseDialog dialog = new MembershipCardPurchaseDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            customer, dataHandler, customerList);
        dialog.setVisible(true);
        
        if (dialog.isPurchaseCompleted()) {
            membershipCardCost = dialog.getCardCost();
            membershipBtn.setEnabled(false);
            membershipBtn.setText("Card Purchased ✓");
            
            // Refresh UI to show points panel
            Container parent = membershipBtn.getParent().getParent().getParent();
            if (parent instanceof JPanel) {
                setupUI(); // Rebuild UI with points panel
                revalidate();
                repaint();
            }
            
            JOptionPane.showMessageDialog(this,
                "Membership card purchased successfully!\nYou can now earn and redeem points.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void updateSummaryDisplay() {
        totalLabel.setText("TOTAL DUE: ₱" + String.format("%.2f", transaction.getFinalTotal()));
    }
    
    private void updatePointsDisplay() {
        if (customer.hasMembership() && pointsToEarnLabel != null) {
            int pointsToEarn = (int) Math.floor(transaction.getFinalTotal() / 50.0);
            pointsToEarnLabel.setText("Points to Earn: " + pointsToEarn + 
                " (₱" + String.format("%.2f", transaction.getFinalTotal()) + " ÷ 50)");
        }
    }
    
    /**
     * Creates the points redemption panel.
     */
    /**
     * Creates the payment details panel.
     */
    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Payment Details"));
        
        // Payment method
        panel.add(new JLabel("Payment Method:"));
        paymentMethodCombo = new JComboBox<>(new String[]{"Cash", "Card"});
        panel.add(paymentMethodCombo);
        
        // Amount paid
        panel.add(new JLabel("Amount Paid:"));
        amountField = new JTextField();
        panel.add(amountField);
        
        // Change (placeholder)
        panel.add(new JLabel("Change:"));
        panel.add(new JLabel("₱0.00"));
        
        return panel;
    }
    
    /**
     * Creates the button panel.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        panel.add(cancelButton);
        
        JButton payButton = new JButton("Complete Payment");
        payButton.setBackground(new Color(34, 139, 34));
        payButton.setForeground(Color.WHITE);
        payButton.addActionListener(e -> processPayment());
        panel.add(payButton);
        
        return panel;
    }
    
    /**
     * Applies points to the transaction.
     */
    private void applyPoints() {
        try {
            int pointsToUse = Integer.parseInt(pointsField.getText());
            int availablePoints = customer.getPoints();
            
            if (pointsToUse < 0) {
                JOptionPane.showMessageDialog(this,
                    "Points must be positive!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (pointsToUse > availablePoints) {
                JOptionPane.showMessageDialog(this,
                    "You only have " + availablePoints + " points!",
                    "Insufficient Points",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 1 point = ₱1 discount
            double discount = customer.usePoints(pointsToUse);
            
            // Ensure discount doesn't exceed the total
            if (discount > transaction.getFinalTotal()) {
                int excessPoints = (int) (discount - transaction.getFinalTotal());
                customer.refundPoints(excessPoints);
                discount = transaction.getFinalTotal();
                pointsToUse -= excessPoints;
            }
            
            // Update transaction with points discount
            transaction.setPointsDiscount(discount);
            transaction.setPointsRedeemed(pointsToUse);
            
            updateSummaryDisplay();
            currentPointsLabel.setText("Current Points: " + customer.getPoints() + 
                " (after redemption: " + (customer.getPoints() + pointsToUse) + " - " + pointsToUse + " used)");
            
            JOptionPane.showMessageDialog(this,
                "Points applied! Discount: ₱" + String.format("%.2f", discount),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid number!",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Processes the payment.
     */
    private void processPayment() {
        try {
            double amountPaid = Double.parseDouble(amountField.getText());
            String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
            
            double totalDue = transaction.getFinalTotal() + membershipCardCost;
            
            if (amountPaid < totalDue) {
                JOptionPane.showMessageDialog(this,
                    "Insufficient payment!\nTotal due: ₱" + String.format("%.2f", totalDue),
                    "Payment Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double change = amountPaid - totalDue;
            transaction.setPaymentMethod(paymentMethod);
            transaction.setAmountPaid(amountPaid);
            transaction.setChange(change);
            
            // Apply points earned
            if (customer.hasMembership()) {
                customer.earnPoints(transaction.getFinalTotal());
            }
            
            // Save customer data
            dataHandler.saveCustomers(customerList);
            
            // Show receipt
            String receiptText = transaction.getReceiptString();
            if (membershipCardCost > 0) {
                receiptText += "\n\n--- Membership Card Purchase ---\n";
                receiptText += "Card Cost: ₱" + String.format("%.2f", membershipCardCost) + "\n";
                receiptText += "Total Paid: ₱" + String.format("%.2f", totalDue) + "\n";
            }
            receiptText += "\nChange: ₱" + String.format("%.2f", change);
            
            JTextArea receiptArea = new JTextArea(receiptText);
            receiptArea.setEditable(false);
            receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            
            JScrollPane scrollPane = new JScrollPane(receiptArea);
            scrollPane.setPreferredSize(new Dimension(400, 500));
            
            JOptionPane.showMessageDialog(this,
                scrollPane,
                "Receipt",
                JOptionPane.INFORMATION_MESSAGE);
            
            transactionCompleted = true;
            dispose();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid amount!",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Returns whether the transaction was completed.
     */
    public boolean isTransactionCompleted() {
        return transactionCompleted;
    }
}