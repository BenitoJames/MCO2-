package view;

import model.Customer;
import util.StoreDataHandler;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dialog for purchasing a membership card.
 */
public class MembershipCardPurchaseDialog extends JDialog {
    private final Customer customer;
    private final StoreDataHandler dataHandler;
    private final List<Customer> customerList;
    private boolean purchaseCompleted;
    private static final double CARD_COST = 50.0;
    
    public MembershipCardPurchaseDialog(Frame parent, Customer customer, 
                                       StoreDataHandler dataHandler, List<Customer> customerList) {
        super(parent, "Purchase Membership Card", true);
        this.customer = customer;
        this.dataHandler = dataHandler;
        this.customerList = customerList;
        this.purchaseCompleted = false;
        
        setupUI();
        setSize(600, 500);
        setLocationRelativeTo(parent);
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(0, 100, 0));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("DLSU Membership Card");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Center panel with card info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        centerPanel.setBackground(Color.WHITE);
        
        // Benefits
        JLabel benefitsTitle = new JLabel("Membership Benefits:");
        benefitsTitle.setFont(new Font("Arial", Font.BOLD, 16));
        benefitsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(benefitsTitle);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        String[] benefits = {
            "• Earn 1 point for every ₱50 spent",
            "• Redeem points: 1 point = ₱1 discount",
            "• Valid for 1 year from purchase date",
            "• Track your purchase history",
            "• Member-exclusive promotions"
        };
        
        for (String benefit : benefits) {
            JLabel benefitLabel = new JLabel(benefit);
            benefitLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            benefitLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerPanel.add(benefitLabel);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Price
        JLabel priceLabel = new JLabel("Card Cost: ₱" + String.format("%.2f", CARD_COST));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 18));
        priceLabel.setForeground(new Color(0, 100, 0));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(priceLabel);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton purchaseBtn = new JButton("Purchase");
        purchaseBtn.setFont(new Font("Arial", Font.BOLD, 14));
        purchaseBtn.setBackground(new Color(34, 139, 34));
        purchaseBtn.setForeground(Color.WHITE);
        purchaseBtn.setFocusPainted(false);
        purchaseBtn.addActionListener(e -> handlePurchase());
        bottomPanel.add(purchaseBtn);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelBtn.addActionListener(e -> {
            purchaseCompleted = false;
            dispose();
        });
        bottomPanel.add(cancelBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void handlePurchase() {
        // Confirm purchase
        int confirm = JOptionPane.showConfirmDialog(this,
            "Purchase membership card for ₱" + String.format("%.2f", CARD_COST) + "?",
            "Confirm Purchase", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Generate card ID
        String cardID = dataHandler.generateMembershipCardID(customerList);
        LocalDate expiryDate = LocalDate.now().plusYears(1);
        
        // Assign card to customer
        customer.assignMembershipCard(cardID, expiryDate);
        
        // Save customer data
        dataHandler.saveCustomers(customerList);
        
        // Show card details
        showCardDetails(cardID, expiryDate);
        
        purchaseCompleted = true;
        dispose();
    }
    
    private void showCardDetails(String cardID, LocalDate expiryDate) {
        JDialog cardDialog = new JDialog(this, "Your Membership Card", true);
        cardDialog.setLayout(new BorderLayout());
        cardDialog.setSize(450, 350);
        cardDialog.setLocationRelativeTo(this);
        
        // Card panel
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(new Color(240, 240, 240));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Card design
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(0, 100, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 70, 0), 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(400, 250));
        
        JLabel cardTitle = new JLabel("DLSU CONVENIENCE STORE");
        cardTitle.setFont(new Font("Arial", Font.BOLD, 16));
        cardTitle.setForeground(Color.WHITE);
        cardTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(cardTitle);
        
        JLabel membershipLabel = new JLabel("MEMBERSHIP CARD");
        membershipLabel.setFont(new Font("Arial", Font.BOLD, 14));
        membershipLabel.setForeground(Color.YELLOW);
        membershipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(membershipLabel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel nameLabel = new JLabel("Name: " + customer.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(nameLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JLabel cardIDLabel = new JLabel("Card No: " + cardID);
        cardIDLabel.setFont(new Font("Courier New", Font.BOLD, 16));
        cardIDLabel.setForeground(Color.YELLOW);
        cardIDLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(cardIDLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        JLabel validLabel = new JLabel("Valid Until: " + expiryDate.format(formatter));
        validLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        validLabel.setForeground(Color.WHITE);
        validLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(validLabel);
        
        JLabel pointsLabel = new JLabel("Points: 0");
        pointsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        pointsLabel.setForeground(Color.WHITE);
        pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(pointsLabel);
        
        cardPanel.add(card);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel infoLabel = new JLabel("Card saved to your account!");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(infoLabel);
        
        cardDialog.add(cardPanel, BorderLayout.CENTER);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.addActionListener(e -> cardDialog.dispose());
        buttonPanel.add(closeBtn);
        cardDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        cardDialog.setVisible(true);
    }
    
    public boolean isPurchaseCompleted() {
        return purchaseCompleted;
    }
    
    public double getCardCost() {
        return CARD_COST;
    }
}
