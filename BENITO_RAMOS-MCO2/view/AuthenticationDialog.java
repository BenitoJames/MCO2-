package view;

import model.Customer;
import util.StoreDataHandler;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog for user authentication (Sign In / Sign Up / Guest).
 */
public class AuthenticationDialog extends JDialog {
    private final StoreDataHandler dataHandler;
    private final List<Customer> customers;
    private Customer authenticatedCustomer;
    private boolean success;
    
    public AuthenticationDialog(Frame parent, StoreDataHandler dataHandler, List<Customer> customers) {
        super(parent, "Customer Authentication", true);
        this.dataHandler = dataHandler;
        this.customers = customers;
        this.authenticatedCustomer = null;
        this.success = false;
        
        setupUI();
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(0, 100, 0));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Welcome to DLSU Convenience Store");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Center panel with buttons
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        centerPanel.setBackground(Color.WHITE);
        
        JLabel instructionLabel = new JLabel("Please choose an option:");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(instructionLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Sign In button
        JButton signInBtn = new JButton("Sign In");
        signInBtn.setFont(new Font("Arial", Font.BOLD, 16));
        signInBtn.setMaximumSize(new Dimension(300, 50));
        signInBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInBtn.setBackground(new Color(34, 139, 34));
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setFocusPainted(false);
        signInBtn.addActionListener(e -> handleSignIn());
        centerPanel.add(signInBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Sign Up button
        JButton signUpBtn = new JButton("Sign Up");
        signUpBtn.setFont(new Font("Arial", Font.BOLD, 16));
        signUpBtn.setMaximumSize(new Dimension(300, 50));
        signUpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpBtn.setBackground(new Color(70, 130, 180));
        signUpBtn.setForeground(Color.WHITE);
        signUpBtn.setFocusPainted(false);
        signUpBtn.addActionListener(e -> handleSignUp());
        centerPanel.add(signUpBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Guest button
        JButton guestBtn = new JButton("Continue as Guest");
        guestBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        guestBtn.setMaximumSize(new Dimension(300, 40));
        guestBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        guestBtn.setBackground(new Color(150, 150, 150));
        guestBtn.setForeground(Color.WHITE);
        guestBtn.setFocusPainted(false);
        guestBtn.addActionListener(e -> handleGuest());
        centerPanel.add(guestBtn);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> {
            success = false;
            dispose();
        });
        bottomPanel.add(cancelBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void handleSignIn() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JTextField userIDField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        
        panel.add(new JLabel("User ID:"));
        panel.add(userIDField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Sign In", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String userID = userIDField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (userID.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both User ID and Password.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Find customer
            Customer foundCustomer = null;
            for (Customer c : customers) {
                if (c.getUserID().equals(userID)) {
                    foundCustomer = c;
                    break;
                }
            }
            
            if (foundCustomer == null) {
                JOptionPane.showMessageDialog(this, "User ID not found.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Verify password
            if (!password.equals(foundCustomer.getPassword())) {
                JOptionPane.showMessageDialog(this, "Incorrect password.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            authenticatedCustomer = foundCustomer;
            success = true;
            dispose();
        }
    }
    
    private void handleSignUp() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        
        JTextField lastNameField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField middleNameField = new JTextField();
        JPasswordField passwordField1 = new JPasswordField();
        JPasswordField passwordField2 = new JPasswordField();
        
        // Generate User ID
        String newUserID = dataHandler.generateUserID(customers);
        JTextField userIDField = new JTextField(newUserID);
        userIDField.setEditable(false);
        userIDField.setBackground(Color.LIGHT_GRAY);
        
        panel.add(new JLabel("User ID:"));
        panel.add(userIDField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);
        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Middle Name (Optional):"));
        panel.add(middleNameField);
        panel.add(new JLabel("Password (min 8 chars):"));
        panel.add(passwordField1);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(passwordField2);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Sign Up", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String lastName = lastNameField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String middleName = middleNameField.getText().trim();
            String password1 = new String(passwordField1.getPassword());
            String password2 = new String(passwordField2.getPassword());
            
            // Validation
            if (lastName.isEmpty() || firstName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Last Name and First Name are required.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password1.length() < 8) {
                JOptionPane.showMessageDialog(this, "Password must be at least 8 characters.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password1.equals(password2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create customer with password
            Customer newCustomer = new Customer(newUserID, lastName, firstName, middleName, password1);
            
            customers.add(newCustomer);
            dataHandler.saveCustomers(customers);
            
            JOptionPane.showMessageDialog(this, 
                "Account created successfully!\nYour User ID is: " + newUserID, 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            authenticatedCustomer = newCustomer;
            success = true;
            dispose();
        }
    }
    
    private void handleGuest() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "As a guest, you will not be able to view purchase history or earn points.\nContinue?",
            "Guest Checkout", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            authenticatedCustomer = new Customer(); // Guest constructor
            success = true;
            dispose();
        }
    }
    
    public Customer getAuthenticatedCustomer() {
        return authenticatedCustomer;
    }
    
    public boolean isSuccess() {
        return success;
    }
}
