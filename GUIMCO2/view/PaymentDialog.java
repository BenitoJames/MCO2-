package view;

import model.*;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for handling payment during checkout.
 */
public class PaymentDialog extends JDialog {
    private final Transaction transaction;
    private final Customer customer;
    private boolean transactionCompleted = false;
    
    private JLabel totalLabel;
    private JTextField pointsField;
    private JComboBox<String> paymentMethodCombo;
    private JTextField amountField;
    
    /**
     * Constructs a payment dialog.
     *
     * @param parent The parent window
     * @param transaction The transaction to process
     * @param customer The customer making the purchase
     */
    public PaymentDialog(Window parent, Transaction transaction, Customer customer) {
        super(parent, "Checkout", ModalityType.APPLICATION_MODAL);
        this.transaction = transaction;
        this.customer = customer;
        
        setupUI();
        setSize(500, 600);
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
        
        // Transaction summary
        centerPanel.add(createSummaryPanel());
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Points redemption (if member)
        if (customer.hasMembership()) {
            centerPanel.add(createPointsPanel());
            centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        }
        
        // Payment details
        centerPanel.add(createPaymentPanel());
        
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
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
     * Creates the points redemption panel.
     */
    private JPanel createPointsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Redeem Points"));
        
        int availablePoints = customer.getMembershipCard().getPoints();
        JLabel pointsLabel = new JLabel("Available Points: " + availablePoints);
        pointsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(pointsLabel, BorderLayout.NORTH);
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Points to use:"));
        
        pointsField = new JTextField(10);
        pointsField.setText("0");
        inputPanel.add(pointsField);
        
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> applyPoints());
        inputPanel.add(applyButton);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
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
            int availablePoints = customer.getMembershipCard().getPoints();
            
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
            
            double newTotal = transaction.redeemPoints(pointsToUse);
            totalLabel.setText("TOTAL DUE: ₱" + String.format("%.2f", newTotal));
            
            JOptionPane.showMessageDialog(this,
                "Points applied! New total: ₱" + String.format("%.2f", newTotal),
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
            
            double change = transaction.processPayment(amountPaid, paymentMethod);
            
            if (change < 0) {
                JOptionPane.showMessageDialog(this,
                    "Insufficient payment!\nTotal due: ₱" + 
                    String.format("%.2f", transaction.getFinalTotal()),
                    "Payment Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Show receipt
            JTextArea receiptArea = new JTextArea(transaction.getReceiptString());
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