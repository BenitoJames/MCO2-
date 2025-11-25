package controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.*;
import util.ConsoleHelper;  // ← ADD THIS LINE
import util.StoreDataHandler;

/**
 * Handles all logic for the employee-facing menu.
 * Includes password-protected login.
 */
public class EmployeeController {

    private Inventory inventory;
    private List<Customer> customerList;
    private StoreDataHandler dataHandler;
    private boolean isRunning;

    /**
     * Constructs an EmployeeController.
     *
     * @param inventory (Inventory) A reference to the main inventory.
     * @param customerList (List<Customer>) A reference to the main customer list.
     * @param dataHandler (StoreDataHandler) A reference to the data handler.
     */
    public EmployeeController(Inventory inventory, List<Customer> customerList, StoreDataHandler dataHandler) {
        this.inventory = inventory;
        this.customerList = customerList;
        this.dataHandler = dataHandler;
        this.isRunning = true;
    }

    /**
     * Asks for the employee password.
     * @return (boolean) True if the password is correct, false otherwise.
     */
    public boolean login() {
        String password = ConsoleHelper.getStringInput("Enter employee password: ");
        // Hard-coded password for MCO1
        boolean success = password.equals("pass123"); 
        return success;
    }

    /**
     * Runs the main loop for the employee menu.
     */
    public void run() {
        while (this.isRunning) {
            showEmployeeMenu();
            int choice = ConsoleHelper.getIntInput("Enter your choice: ", 0, 7);
            switch (choice) {
                case 1:
                    handleViewInventory();
                    break;
                case 2:
                    handleAddNewProduct();
                    break;
                case 3:
                    handleRestockProduct();
                    break;
                case 4:
                    handleViewLowStock();
                    break;
                case 5:
                    handleManageExpiring();
                    break;
                case 6:
                    handleManageCustomers();
                    break;
                case 7:
                    handleViewSalesLog();
                    break;
                case 0:
                    this.isRunning = false;
                    break;
            }
        }
    }

    /**
     * Displays the employee menu options.
     */
    private void showEmployeeMenu() {
        System.out.println("\n--- Employee Menu ---");
        System.out.println("1. View Full Inventory");
        System.out.println("2. Add New Product");
        System.out.println("3. Restock Product");
        System.out.println("4. View Low-Stock Items");
        System.out.println("5. Manage Expiring Items");
        System.out.println("6. Manage Customers");
        System.out.println("7. View Sales Log");
        System.out.println("0. Logout");
    }


    /**
     * Displays the entire inventory.
     */
    private void handleViewInventory() {
        System.out.println("\n--- Full Store Inventory ---");
        // This will now work, as Shelf.getDisplayString() accepts a title
        System.out.println(inventory.viewAllInventory());
    }

    /**
     * Guides the employee through adding a new product to the inventory.
     */
    private void handleAddNewProduct() {
        System.out.println("\n--- Add New Product ---");
        
        System.out.println("Select Product Category Type:");
        System.out.println("1. Food");
        System.out.println("2. Beverages");
        System.out.println("3. Pharmacy");
        System.out.println("4. Toiletries");
        System.out.println("5. Household & Pet");
        System.out.println("6. General & Specialty");
        int typeChoice = ConsoleHelper.getIntInput("Enter category type: ", 1, 6);

        String productID = ConsoleHelper.getStringInput("Enter Product ID (e.g., F001): ");
        String name = ConsoleHelper.getStringInput("Enter Product Name: ");
        double price = ConsoleHelper.getDoubleInput("Enter Price: ", 0.01);
        int quantityInStock = ConsoleHelper.getIntInput("Enter Initial Stock: ", 1, Integer.MAX_VALUE);
        List<String> subcategories = getSubcategoriesInput();
        String brand = ConsoleHelper.getStringInput("Enter Brand (or leave empty): ");
        String variant = ConsoleHelper.getStringInput("Enter Variant (or leave empty): ");

        Product newProduct = null;
        boolean isPerishable = (typeChoice >= 1 && typeChoice <= 3);

        if (isPerishable) {
            LocalDate expirationDate = ConsoleHelper.getDateInput("Enter Expiration Date");
            
            switch (typeChoice) {
                case 1:
                    newProduct = new Food(productID, name, price, quantityInStock, subcategories, brand, variant, expirationDate);
                    break;
                case 2:
                    newProduct = new Beverages(productID, name, price, quantityInStock, subcategories, brand, variant, expirationDate);
                    break;
                case 3:
                    newProduct = new Pharmacy(productID, name, price, quantityInStock, subcategories, brand, variant, expirationDate);
                    break;
            }
        } else {
            switch (typeChoice) {
                case 4:
                    newProduct = new Toiletries(productID, name, price, quantityInStock, subcategories, brand, variant);
                    break;
                case 5:
                    newProduct = new HouseholdAndPet(productID, name, price, quantityInStock, subcategories, brand, variant);
                    break;
                case 6:
                    newProduct = new GeneralAndSpecialty(productID, name, price, quantityInStock, subcategories, brand, variant);
                    break;
            }
        }

        if (newProduct != null) {
            inventory.addProduct(newProduct);
            System.out.println("Successfully added new product: " + name);
        } else {
            System.out.println("Error: Could not create product.");
        }
    }
    
    private List<String> getSubcategoriesInput() {
        List<String> subcategories = new ArrayList<>();
        System.out.println("Enter subcategories (e.g., 'Food', 'Snack'). Type 'done' when finished.");
        String subcategory = "";
        boolean isDone = false;
        while (!isDone) {
            subcategory = ConsoleHelper.getStringInput("Subcategory: ");
            if (subcategory.equalsIgnoreCase("done")) {
                isDone = true;
            } else {
                subcategories.add(subcategory);
            }
        }
        return subcategories;
    }

    /**
     * Handles restocking an existing product.
     */
    private void handleRestockProduct() {
        System.out.println("\n--- Restock Product ---");
        String productID = ConsoleHelper.getStringInput("Enter Product ID to restock: ");
        
        Product product = inventory.findProductByID(productID);
        
        if (product == null) {
            System.out.println("Error: Product not found.");
            return;
        }

        System.out.println("Current stock for " + product.getName() + ": " + product.getQuantityInStock());
        int amountToAdd = ConsoleHelper.getIntInput("Enter amount to add: ", 1, Integer.MAX_VALUE);
        
        product.setQuantityInStock(product.getQuantityInStock() + amountToAdd);
        
        System.out.println("Successfully restocked. New quantity: " + product.getQuantityInStock());
    }

    /**
     * Views all items with stock at 5 or less.
     */
    private void handleViewLowStock() {
        System.out.println("\n--- Low-Stock Items (5 or less) ---");
        // This will now work, as Shelf.getLowStockItems() accepts an int
        List<Product> lowStockItems = inventory.getLowStockItems();
        
        if (lowStockItems.isEmpty()) {
            System.out.println("No low-stock items found.");
        } else {
            for (Product p : lowStockItems) {
                System.out.println(p.displayDetails());
            }
        }
    }

    /**
     * Views and optionally removes expiring or expired items.
     */
    private void handleManageExpiring() {
        System.out.println("\n--- Expiring / Expired Items ---");
        List<Product> expiringItems = inventory.getExpiringItems();
        
        if (expiringItems.isEmpty()) {
            System.out.println("No expiring items found.");
            return;
        }
        
        System.out.println("The following items are expired or will expire within 7 days:");
        for (Product p : expiringItems) {
            System.out.println(p.displayDetails());
        }
        
        boolean confirm = ConsoleHelper.getYesNoInput("\nDo you want to remove all these items from inventory?");
        if (confirm) {
            List<Product> removedItems = inventory.removeExpiringItems();
            System.out.println("Successfully removed " + removedItems.size() + " expiring items.");
        }
    }
    
    /**
     * Shows the customer management sub-menu.
     */
    private void handleManageCustomers() {
        System.out.println("\n--- Customer Management ---");
        System.out.println("1. Add New Customer");
        System.out.println("2. View All Customers");
        System.out.println("3. Toggle Senior/PWD Status");
        System.out.println("0. Back to Employee Menu");
        
        int choice = ConsoleHelper.getIntInput("Enter choice: ", 0, 3);
        
        switch (choice) {
            case 1:
                handleAddCustomer();
                break;
            case 2:
                handleViewCustomers();
                break;
            case 3:
                handleToggleSeniorStatus();
                break;
            case 0:
                break;
        }
    }

    /**
     * Handles adding a new customer to the customer list.
     */
    private void handleAddCustomer() {
        System.out.println("\n--- Add New Customer ---");
        String id = ConsoleHelper.getStringInput("Enter new Customer ID: ");
        String name = ConsoleHelper.getStringInput("Enter Customer Name: ");
        
        Customer newCustomer = new Customer(id, name);
        
        boolean hasCard = ConsoleHelper.getYesNoInput("Assign a membership card?");
        if (hasCard) {
            // --- FIXED CONSTRUCTOR (Error 10) ---
            // Call the constructor with 0 initial points
            newCustomer.assignMembershipCard(new MembershipCard(0));
        }
        
        customerList.add(newCustomer);
        System.out.println("Successfully added customer: " + name);
    }

    /**
     * Displays all registered customers.
     */
    private void handleViewCustomers() {
        System.out.println("\n--- All Customers ---");
        if (customerList.isEmpty()) {
            System.out.println("No customers registered.");
        } else {
            for (Customer c : customerList) {
                System.out.println(c.toString());
            }
        }
    }

    /**
     * Toggles the Senior/PWD status for a customer.
     */
    private void handleToggleSeniorStatus() {
        System.out.println("\n--- Toggle Senior/PWD Status ---");
        String id = ConsoleHelper.getStringInput("Enter Customer ID: ");
        
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

        if (foundCustomer == null) {
            System.out.println("Error: Customer not found.");
            return;
        }

        boolean newStatus = !foundCustomer.getIsSenior();
        foundCustomer.setIsSenior(newStatus);
        
        System.out.println("Successfully updated " + foundCustomer.getName() + ".");
        System.out.println("New Senior/PWD Status: " + newStatus);
    }

    /**
     * Reads and displays the sales transaction log.
     */
    /**
 * Reads and displays the sales transaction log.
 */
private void handleViewSalesLog() {
    System.out.println("\n--- Sales Transaction Log ---");
    
    try {
        java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.FileReader("sales_log.txt")
        );
        String line;
        boolean hasData = false;
        
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            hasData = true;
        }
        reader.close();
        
        if (!hasData) {
            System.out.println("No transactions found in log.");
        }
    } catch (java.io.IOException e) {
        System.out.println("Error reading sales log: " + e.getMessage());
    }
}
}

