package controller;

import java.util.List;
import javax.swing.*;
import model.*;
import util.*;
import view.*;

/**
 * Main GUI controller for the convenience store application.
 * Manages navigation between different screens and coordinates
 * interactions between the model and view layers.
 */
public class StoreControllerGUI {
    private final MainWindow mainWindow;
    private Inventory inventory;
    private List<Customer> customerList;
    private final StoreDataHandler dataHandler;
    private final SaleManager saleManager;
    
    // View components
    private final StoreGUI storeGUI;
    private CustomerGUI customerGUI;
    private EmployeeGUI employeeGUI;
    
    // Current state
    private Customer currentCustomer;
    
    /**
     * Constructs the main GUI controller.
     * Initializes all data and view components.
     *
     * @param mainWindow The main application window
     */
    public StoreControllerGUI(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.dataHandler = new StoreDataHandler();
        this.saleManager = new SaleManager();
        
        // Load data from files
        this.inventory = dataHandler.loadInventory();
        this.customerList = dataHandler.loadCustomers();
        
        // Initialize main store screen
        this.storeGUI = new StoreGUI(this);
        
        // Add screens to main window
        mainWindow.addScreen(storeGUI, "STORE");
        
        // Show the store screen
        mainWindow.showScreen("STORE");
        
        // Add window closing handler to save data
        setupWindowClosing();
    }
    
    /**
     * Sets up the window closing handler to save data on exit.
     */
    private void setupWindowClosing() {
        mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainWindow.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleExit();
            }
        });
    }
    
    /**
     * Handles the customer shopping flow with authentication.
     * Shows Sign In / Sign Up / Guest dialog.
     */
    public void startCustomerShopping() {
        // Show authentication dialog
        AuthenticationDialog authDialog = new AuthenticationDialog(mainWindow, dataHandler, customerList);
        authDialog.setVisible(true);
        
        if (!authDialog.isSuccess()) {
            return; // User cancelled
        }
        
        currentCustomer = authDialog.getAuthenticatedCustomer();
        
        // Create and show customer GUI
        customerGUI = new CustomerGUI(this, inventory, currentCustomer);
        mainWindow.addScreen(customerGUI, "CUSTOMER");
        mainWindow.showScreen("CUSTOMER");
    }
    
    /**
     * Handles the employee login and access.
     */
    public void startEmployeeMode() {
        // Password dialog
        JPasswordField passwordField = new JPasswordField(20);
        int option = JOptionPane.showConfirmDialog(
            mainWindow,
            passwordField,
            "Employee Login - Enter Password",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            
            // Hard-coded password (as per MCO1 specs)
            if (password.equals("pass123")) {
                JOptionPane.showMessageDialog(
                    mainWindow,
                    "Login successful!",
                    "Welcome",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                // Create and show employee GUI
                employeeGUI = new EmployeeGUI(this, inventory, customerList, saleManager);
                mainWindow.addScreen(employeeGUI, "EMPLOYEE");
                mainWindow.showScreen("EMPLOYEE");
            } else {
                JOptionPane.showMessageDialog(
                    mainWindow,
                    "Incorrect password. Access denied.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Returns to the main store screen.
     */
    public void returnToMainMenu() {
        mainWindow.showScreen("STORE");
    }
    
    /**
     * Completes a customer transaction and saves data.
     *
     * @param transaction The completed transaction
     */
    public void completeTransaction(Transaction transaction) {
        // Save receipt and transaction log
        dataHandler.saveReceipt(transaction.getReceiptString());
        dataHandler.saveSalesLog(transaction.getTransactionSummary());
        
        // Save updated inventory and customer data
        saveAllData();
        
        // Show success message
        JOptionPane.showMessageDialog(
            mainWindow,
            "Transaction completed successfully!\nThank you for shopping with us!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // Return to main menu
        returnToMainMenu();
    }
    
    /**
     * Cancels customer shopping and returns stock.
     */
    public void cancelCustomerShopping() {
        int confirm = JOptionPane.showConfirmDialog(
            mainWindow,
            "Are you sure you want to cancel shopping?\nItems in your cart will be returned to inventory.",
            "Cancel Shopping",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            returnToMainMenu();
        }
    }
    
    /**
     * Saves inventory data when employee makes changes.
     */
    public void saveInventoryChanges() {
        dataHandler.saveInventory(inventory);
        JOptionPane.showMessageDialog(
            mainWindow,
            "Inventory changes saved successfully!",
            "Saved",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Saves customer list when employee makes changes.
     */
    public void saveCustomerChanges() {
        dataHandler.saveCustomers(customerList);
        JOptionPane.showMessageDialog(
            mainWindow,
            "Customer changes saved successfully!",
            "Saved",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Gets the sale manager.
     */
    public SaleManager getSaleManager() {
        return saleManager;
    }
    
    /**
     * Saves all data (inventory, customers).
     */
    public void saveAllData() {
        dataHandler.saveInventory(inventory);
        dataHandler.saveCustomers(customerList);
    }
    
    /**
     * Displays the sales log in a dialog.
     */
    public void showSalesLog() {
        // Create a text area to display the log
        JTextArea textArea = new JTextArea(20, 50);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        
        // Read sales log
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader("sales_log.txt")
            );
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            
            if (content.length() == 0) {
                textArea.setText("No sales transactions recorded.");
            } else {
                textArea.setText(content.toString());
            }
        } catch (java.io.IOException e) {
            textArea.setText("Error reading sales log: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(
            mainWindow,
            scrollPane,
            "Sales Log",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Handles application exit with save confirmation.
     */
    public void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
            mainWindow,
            "Do you want to save changes before exiting?",
            "Exit Application",
            JOptionPane.YES_NO_CANCEL_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            saveAllData();
            System.exit(0);
        } else if (confirm == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
        // If CANCEL, do nothing (stay in application)
    }
    
    // --- Helper Methods ---
    
    /**
     * Finds a customer by their ID.
     *
     * @param customerID The customer ID to search for
     * @return The Customer object if found, null otherwise
     */
    private Customer findCustomerByID(String customerID) {
        for (Customer c : customerList) {
            if (c.getCustomerID().equalsIgnoreCase(customerID)) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * Creates a guest customer with a unique ID.
     * Note: This method is kept for backward compatibility but guests now use Customer() constructor.
     *
     * @return A new guest Customer object
     */
    private Customer createGuestCustomer() {
        return new Customer(); // Guest constructor
    }
    
    // --- Getters ---
    
    public MainWindow getMainWindow() {
        return mainWindow;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public List<Customer> getCustomerList() {
        return customerList;
    }
    
    public StoreDataHandler getDataHandler() {
        return dataHandler;
    }
}