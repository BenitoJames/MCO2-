package view;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import model.Customer;
import util.StoreDataHandler;

/**
 * Dialog for editing user/customer details.
 * Staff can modify points and membership status.
 */
public class EditUserDialog extends JDialog {
    private final Customer customer;
    private final StoreDataHandler dataHandler;
    private final List<Customer> customerList;
    private boolean changesSaved = false;
    
    private JTextField pointsField;
    private JTextField membershipCardField;
    private JSpinner expiryDateSpinner;
    private JCheckBox activeMembershipCheckbox;
    
    /**
     * Constructs the Edit User dialog.
     *
     * @param parent The parent window
     * @param customer The customer to edit
     * @param dataHandler The data handler for saving
     * @param customerList The list of all customers
     */
    public EditUserDialog(Window parent, Customer customer, StoreDataHandler dataHandler, List<Customer> customerList) {
        super(parent, "Edit User: " + customer.getName(), ModalityType.APPLICATION_MODAL);
        this.customer = customer;
        this.dataHandler = dataHandler;
        this.customerList = customerList;
        
        setupUI();
        setSize(500, 450);
        setLocationRelativeTo(parent);
    }
    
    /**
     * Sets up the user interface.
     */
    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(33, 150, 243));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Edit User Details");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // User ID (read-only)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("User ID:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField userIDField = new JTextField(customer.getUserID());
        userIDField.setEditable(false);
        userIDField.setBackground(Color.LIGHT_GRAY);
        formPanel.add(userIDField, gbc);
        
        row++;
        
        // Name (read-only)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(customer.getName());
        nameField.setEditable(false);
        nameField.setBackground(Color.LIGHT_GRAY);
        formPanel.add(nameField, gbc);
        
        row++;
        
        // Separator
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JSeparator separator = new JSeparator();
        formPanel.add(separator, gbc);
        gbc.gridwidth = 1;
        
        row++;
        
        // Points (editable)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Points:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pointsField = new JTextField(String.valueOf(customer.getPoints()), 10);
        pointsPanel.add(pointsField);
        JButton addPointsBtn = new JButton("+100");
        addPointsBtn.addActionListener(e -> adjustPoints(100));
        pointsPanel.add(addPointsBtn);
        JButton subtractPointsBtn = new JButton("-100");
        subtractPointsBtn.addActionListener(e -> adjustPoints(-100));
        pointsPanel.add(subtractPointsBtn);
        formPanel.add(pointsPanel, gbc);
        
        row++;
        
        // Membership section title
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JLabel membershipLabel = new JLabel("Membership Card:");
        membershipLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(membershipLabel, gbc);
        gbc.gridwidth = 1;
        
        row++;
        
        // Active membership checkbox
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        activeMembershipCheckbox = new JCheckBox("Has Active Membership");
        activeMembershipCheckbox.setSelected(customer.getMembershipCardID() != null);
        activeMembershipCheckbox.addActionListener(e -> toggleMembershipFields());
        formPanel.add(activeMembershipCheckbox, gbc);
        gbc.gridwidth = 1;
        
        row++;
        
        // Membership Card ID
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Card ID:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        membershipCardField = new JTextField(
            customer.getMembershipCardID() != null ? customer.getMembershipCardID() : ""
        );
        formPanel.add(membershipCardField, gbc);
        
        row++;
        
        // Expiry Date
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Expiry Date:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel expiryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        LocalDate currentExpiry = customer.getCardExpiryDate() != null ? 
            customer.getCardExpiryDate() : LocalDate.now().plusYears(1);
        
        SpinnerDateModel dateModel = new SpinnerDateModel();
        expiryDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(expiryDateSpinner, "yyyy-MM-dd");
        expiryDateSpinner.setEditor(dateEditor);
        expiryDateSpinner.setValue(java.sql.Date.valueOf(currentExpiry));
        
        expiryPanel.add(expiryDateSpinner);
        
        JButton extendBtn = new JButton("Extend +1 Year");
        extendBtn.addActionListener(e -> extendMembership());
        expiryPanel.add(extendBtn);
        
        formPanel.add(expiryPanel, gbc);
        
        row++;
        
        // Info label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("<html><i>Note: Changes to membership will be saved immediately.</i></html>");
        infoLabel.setForeground(Color.GRAY);
        formPanel.add(infoLabel, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(new Color(76, 175, 80));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> handleSave());
        buttonPanel.add(saveBtn);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(cancelBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize field states
        toggleMembershipFields();
    }
    
    /**
     * Adjusts points by the given amount.
     */
    private void adjustPoints(int amount) {
        try {
            int currentPoints = Integer.parseInt(pointsField.getText());
            int newPoints = Math.max(0, currentPoints + amount);
            pointsField.setText(String.valueOf(newPoints));
        } catch (NumberFormatException ex) {
            pointsField.setText("0");
        }
    }
    
    /**
     * Extends membership by 1 year.
     */
    private void extendMembership() {
        java.util.Date currentDate = (java.util.Date) expiryDateSpinner.getValue();
        LocalDate localDate = new java.sql.Date(currentDate.getTime()).toLocalDate();
        LocalDate newDate = localDate.plusYears(1);
        expiryDateSpinner.setValue(java.sql.Date.valueOf(newDate));
    }
    
    /**
     * Toggles membership field states.
     */
    private void toggleMembershipFields() {
        boolean hasCard = activeMembershipCheckbox.isSelected();
        membershipCardField.setEnabled(hasCard);
        expiryDateSpinner.setEnabled(hasCard);
        
        if (hasCard && membershipCardField.getText().trim().isEmpty()) {
            // Generate new card ID if enabling and no ID exists
            String newCardID = dataHandler.generateMembershipCardID(customerList);
            membershipCardField.setText(newCardID);
        }
    }
    
    /**
     * Handles saving changes.
     */
    private void handleSave() {
        try {
            // Validate and update points
            int newPoints = Integer.parseInt(pointsField.getText().trim());
            if (newPoints < 0) {
                JOptionPane.showMessageDialog(this,
                    "Points cannot be negative!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Update points
            int currentPoints = customer.getPoints();
            int pointsDiff = newPoints - currentPoints;
            if (pointsDiff > 0) {
                customer.earnPoints(pointsDiff * 50); // Simulate earning
            } else if (pointsDiff < 0) {
                customer.usePoints(-pointsDiff);
            }
            
            // Update membership
            if (activeMembershipCheckbox.isSelected()) {
                String cardID = membershipCardField.getText().trim();
                if (cardID.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Membership Card ID cannot be empty!",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                java.util.Date expiryDate = (java.util.Date) expiryDateSpinner.getValue();
                LocalDate localExpiry = new java.sql.Date(expiryDate.getTime()).toLocalDate();
                
                customer.assignMembershipCard(cardID, localExpiry);
            } else {
                // Remove membership
                customer.assignMembershipCard(null, null);
            }
            
            // Save to file
            dataHandler.saveCustomers(customerList);
            
            changesSaved = true;
            JOptionPane.showMessageDialog(this,
                "Changes saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid points value!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Returns whether changes were saved.
     */
    public boolean isChangesSaved() {
        return changesSaved;
    }
}
