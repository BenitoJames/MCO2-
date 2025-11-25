package model;

import java.util.List;

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
     * Handles the "Guest Checkout" flow for a customer.
     */
    private void handleCustomerShopping() {
        System.out.println("\n--- Welcome, Customer! ---");

        // 1. Create a "Guest" customer by default
        Customer currentCustomer = new Customer("GUEST-001", "Guest");

        // 2. Ask for Senior/PWD status
        boolean isSenior = ConsoleHelper.getYesNoInput("Are you a Senior Citizen or PWD?");
        currentCustomer.setIsSenior(isSenior);

        // 3. Optional: Log in as a member
        boolean wantsToLogin = ConsoleHelper.getYesNoInput("Do you have a membership card? (y/n)");
        if (wantsToLogin) {
            Customer member = handleMemberLogin();
            if (member != null) {
                // If login is successful, use the member's profile
                // We must transfer the senior status set earlier
                member.setIsSenior(isSenior);
                currentCustomer = member; 
            }
        }

        // 4. Start the customer controller
        CustomerController customerApp = new CustomerController(inventory, currentCustomer, dataHandler);
        customerApp.run();
    }

    /**
     * Handles the specific process of logging in a member.
     * @return (Customer) The found customer, or null if not found/aborted.
     */
    private Customer handleMemberLogin() {
        String id = ConsoleHelper.getStringInput("Enter your Customer ID: ");
        Customer foundCustomer = null;
        boolean found = false;
        int i = 0;
        
        while (i < customerList.size() && !found) {
            if (customerList.get(i).getCustomerID().equalsIgnoreCase(id)) {
                foundCustomer = customerList.get(i);
                found = true;
            }
            i++;
        }

        if (foundCustomer != null) {
            System.out.println("Welcome back, " + foundCustomer.getName() + "!");
        } else {
            System.out.println("Customer ID not found. Proceeding as Guest.");
        }
        return foundCustomer;
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

