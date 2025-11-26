package view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import model.*;

/**
 * Dialog for adding a new user.
 */
public class AddCustomerDialog extends JDialog {
    private final List<Customer> customerList;
    private boolean customerAdded = false;
    
    private JTextField lastNameField;
    private JTextField firstNameField;
    private JTextField middleNameField;
    
    /**
     * Constructs an add user dialog.
     *
     * @param parent The parent window
     * @param customerList The customer list to add to
     */
    public AddCustomerDialog(Window parent, List<Customer> customerList) {
        super(parent, "Add New User", ModalityType.APPLICATION_MODAL);
        this.customerList = customerList;
        
        setupUI();
        setSize(400, 300);
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
        
        JLabel titleLabel = new JLabel("Add New User");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Last Name
        formPanel.add(new JLabel("Last Name: *"));
        lastNameField = new JTextField();
        formPanel.add(lastNameField);
        
        // First Name
        formPanel.add(new JLabel("First Name: *"));
        firstNameField = new JTextField();
        formPanel.add(firstNameField);
        
        // Middle Name
        formPanel.add(new JLabel("Middle Name:"));
        middleNameField = new JTextField();
        formPanel.add(middleNameField);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        JButton addButton = new JButton("Add User");
        addButton.setBackground(new Color(34, 139, 34));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addCustomer());
        buttonPanel.add(addButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Adds the user to the list.
     */
    private void addCustomer() {
        try {
            String lastName = lastNameField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String middleName = middleNameField.getText().trim();
            
            if (lastName.isEmpty() || firstName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Last Name and First Name are required!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Auto-generate user ID
            String userID = generateUserID();
            
            // Default password
            String password = "DLSUser2025";
            
            // Create customer
            Customer customer = new Customer(userID, lastName, firstName, middleName, password);
            
            customerList.add(customer);
            customerAdded = true;
            
            JOptionPane.showMessageDialog(this,
                "User added successfully!\n" +
                "User ID: " + userID + "\n" +
                "Default Password: " + password,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error adding user: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generates a new user ID in the format DLSUser-XXX.
     * @return (String) The generated user ID.
     */
    private String generateUserID() {
        int maxNumber = 0;
        for (Customer customer : customerList) {
            String id = customer.getUserID();
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
     * Returns whether a customer was added.
     */
    public boolean isCustomerAdded() {
        return customerAdded;
    }
}