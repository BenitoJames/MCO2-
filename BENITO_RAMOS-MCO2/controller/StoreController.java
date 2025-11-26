package controller;

import java.util.List;
import model.*;
import util.*;

/**
 * The main application class. Loads data, manages the main menu,
 * and delegates to sub-controllers (Employee or Customer).
 */
public class StoreController {

    private Inventory inventory;
    private List<Customer> customerList;
    private StoreDataHandler dataHandler;
    private boolean isRunning;

    /**
     * Constructs the StoreController, initializing the application's state
     * by loading data from files.
     */
    public StoreController() {
        this.dataHandler = new StoreDataHandler();
        this.inventory = dataHandler.loadInventory();
        this.customerList = dataHandler.loadCustomers();
        this.isRunning = true;
    }

    /**
     * Starts the main application loop.
     */
    public void start() {
        while (this.isRunning) {
            showMainMenu();
            int choice = ConsoleHelper.getIntInput("Enter your choice: ", 0, 2);
            switch (choice) {
                case 1:
                    handleEmployeeLogin();
                    break;
                case 2:
                    handleCustomerShopping();
                    break;
                case 0:
                    shutdown();
                    break;
            }
        }
    }

    /**
     * Displays the main menu (Employee vs. Customer).
     */
    private void showMainMenu() {
        System.out.println("\n--- DLSU Convenience Store ---");
        System.out.println("1. Employee Login");
        System.out.println("2. Customer Shopping");
        System.out.println("0. Exit Application");
    }

    /**
     * Handles the employee login process.
     * Calls the EmployeeController's login method.
     */
    private void handleEmployeeLogin() {
        System.out.println("\n--- Employee Login ---");
        EmployeeController employeeApp = new EmployeeController(inventory, customerList, dataHandler);
        
        // Call the login method
        boolean isLoggedIn = employeeApp.login();
        
        if (isLoggedIn) {
            System.out.println("Login successful.");
            employeeApp.run();
        } else {
            System.out.println("Login failed. Returning to main menu.");
        }
    }

    /**
     * Handles the customer authentication and shopping flow.
     */
    private void handleCustomerShopping() {
        System.out.println("\n--- Welcome, Customer! ---");
        System.out.println("1. Sign In");
        System.out.println("2. Sign Up");
        System.out.println("3. Continue as Guest");
        System.out.println("0. Back to Main Menu");
        
        int choice = ConsoleHelper.getIntInput("Enter your choice: ", 0, 3);
        
        Customer currentCustomer = null;
        
        switch (choice) {
            case 1:
                currentCustomer = handleSignIn();
                break;
            case 2:
                currentCustomer = handleSignUp();
                break;
            case 3:
                currentCustomer = createGuestCustomer();
                System.out.println("Continuing as guest.");
                break;
            case 0:
                return;
        }
        
        if (currentCustomer != null) {
            // Start the customer controller
            CustomerController customerApp = new CustomerController(inventory, currentCustomer, dataHandler, customerList);
            customerApp.run();
            
            // Save customer data after shopping (points may have changed)
            dataHandler.saveCustomers(customerList);
        }
    }

    /**
     * Handles the sign in process.
     * @return (Customer) The authenticated customer, or null if authentication failed.
     */
    private Customer handleSignIn() {
        System.out.println("\n--- Sign In ---");
        String userID = ConsoleHelper.getStringInput("Enter User ID: ");
        
        Customer foundCustomer = null;
        for (Customer customer : customerList) {
            if (customer.getUserID().equals(userID)) {
                foundCustomer = customer;
                break;
            }
        }
        
        if (foundCustomer == null) {
            System.out.println("User ID not found.");
            return null;
        }
        
        String password = ConsoleHelper.getStringInput("Enter Password: ");
        
        if (foundCustomer.getPassword().equals(password)) {
            System.out.println("Welcome back, " + foundCustomer.getName() + "!");
            return foundCustomer;
        } else {
            System.out.println("Incorrect password.");
            return null;
        }
    }

    /**
     * Handles the sign up process for new users.
     * @return (Customer) The newly created customer, or null if sign up was cancelled.
     */
    private Customer handleSignUp() {
        System.out.println("\n--- Sign Up ---");
        
        String lastName = ConsoleHelper.getStringInput("Enter Last Name: ");
        String firstName = ConsoleHelper.getStringInput("Enter First Name: ");
        String middleName = ConsoleHelper.getStringInput("Enter Middle Name: ");
        
        String password;
        while (true) {
            password = ConsoleHelper.getStringInput("Enter Password (min 8 characters): ");
            if (password.length() >= 8) {
                break;
            }
            System.out.println("Password must be at least 8 characters long.");
        }
        
        String confirmPassword = ConsoleHelper.getStringInput("Confirm Password: ");
        
        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match. Sign up cancelled.");
            return null;
        }
        
        // Generate user ID
        String userID = generateUserID();
        
        Customer newCustomer = new Customer(userID, lastName, firstName, middleName, password);
        customerList.add(newCustomer);
        
        System.out.println("Account created successfully!");
        System.out.println("Your User ID: " + userID);
        System.out.println("Welcome, " + newCustomer.getName() + "!");
        
        return newCustomer;
    }

    /**
     * Creates a guest customer.
     * @return (Customer) A guest customer instance.
     */
    private Customer createGuestCustomer() {
        return new Customer("GUEST-" + System.currentTimeMillis(), "Guest");
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
     * Shuts down the application and saves all data.
     */
    private void shutdown() {
        System.out.println("Shutting down...");
        dataHandler.saveInventory(inventory);
        dataHandler.saveCustomers(customerList);
        ConsoleHelper.closeScanner();
        this.isRunning = false;
        System.out.println("Application closed.");
    }
}