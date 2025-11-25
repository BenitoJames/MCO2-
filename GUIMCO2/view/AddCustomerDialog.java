package view;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog for adding a new customer.
 */
public class AddCustomerDialog extends JDialog {
    private final List<Customer> customerList;
    private boolean customerAdded = false;
    
    private JTextField idField;
    private JTextField nameField;
    private JCheckBox seniorCheckBox;
    private JCheckBox membershipCheckBox;
    private JTextField pointsField;
    
    /**
     * Constructs an add customer dialog.
     *
     * @param parent The parent window
     * @param customerList The customer list to add to
     */
    public AddCustomerDialog(Window parent, List<Customer> customerList) {
        super(parent, "Add New Customer", ModalityType.APPLICATION_MODAL);
        this.customerList = customerList;
        
        setupUI();
        setSize(400, 350);
        setLocationRelativeTo(parent);
    }
    
    /**
     * Sets up the dialog UI.
     */
    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(25, 118, 210));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Add New Customer");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Customer ID
        formPanel.add(new JLabel("Customer ID:"));
        idField = new JTextField();
        formPanel.add(idField);
        
        // Name
        formPanel.add(new JLabel("Customer Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);
        
        // Senior/PWD
        formPanel.add(new JLabel("Senior/PWD:"));
        seniorCheckBox = new JCheckBox();
        formPanel.add(seniorCheckBox);
        
        // Membership
        formPanel.add(new JLabel("Has Membership:"));
        membershipCheckBox = new JCheckBox();
        membershipCheckBox.addActionListener(e -> pointsField.setEnabled(membershipCheckBox.isSelected()));
        formPanel.add(membershipCheckBox);
        
        // Initial Points
        formPanel.add(new JLabel("Initial Points:"));
        pointsField = new JTextField("0");
        pointsField.setEnabled(false);
        formPanel.add(pointsField);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        JButton addButton = new JButton("Add Customer");
        addButton.setBackground(new Color(34, 139, 34));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addCustomer());
        buttonPanel.add(addButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Adds the customer to the list.
     */
    private void addCustomer() {
        try {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            
            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "ID and Name are required!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check for duplicate ID
            for (Customer c : customerList) {
                if (c.getCustomerID().equalsIgnoreCase(id)) {
                    JOptionPane.showMessageDialog(this,
                        "Customer ID already exists!",
                        "Duplicate ID",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Create customer
            Customer customer = new Customer(id, name);
            customer.setIsSenior(seniorCheckBox.isSelected());
            
            // Add membership if checked
            if (membershipCheckBox.isSelected()) {
                int points = Integer.parseInt(pointsField.getText());
                customer.assignMembershipCard(new MembershipCard(points));
            }
            
            customerList.add(customer);
            customerAdded = true;
            
            JOptionPane.showMessageDialog(this,
                "Customer added successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Points must be a valid number!",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Returns whether a customer was added.
     */
    public boolean isCustomerAdded() {
        return customerAdded;
    }
}