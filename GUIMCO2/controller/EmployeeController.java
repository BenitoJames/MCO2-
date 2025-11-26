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
            int choice = ConsoleHelper.getIntInput("Enter your choice: ", 0, 9);
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
                    handleUpdateProductPrice();
                    break;
                case 5:
                    handleUpdateProductInfo();
                    break;
                case 6:
                    handleViewLowStock();
                    break;
                case 7:
                    handleManageExpiring();
                    break;
                case 8:
                    handleManageCustomers();
                    break;
                case 9:
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
        System.out.println("4. Update Product Price");
        System.out.println("5. Update Product Information");
        System.out.println("6. View Low-Stock Items");
        System.out.println("7. Manage Expiring Items");
        System.out.println("8. Manage Customers");
        System.out.println("9. View Sales Log");
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

        // Generate Product ID based on category
        String productID = generateProductID(typeChoice);
        System.out.println("Generated Product ID: " + productID);
        
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
            dataHandler.saveInventory(inventory);
            System.out.println("Successfully added new product: " + name);
        } else {
            System.out.println("Error: Could not create product.");
        }
    }

    /**
     * Generates a Product ID based on category type.
     * Format: {CategoryPrefix}-{NextNumber}
     * Example: F-006, B-010, P-008
     *
     * @param categoryType (int) The category choice (1-6).
     * @return (String) The generated Product ID.
     */
    private String generateProductID(int categoryType) {
        String prefix;
        switch (categoryType) {
            case 1:
                prefix = "F";
                break;
            case 2:
                prefix = "B";
                break;
            case 3:
                prefix = "P";
                break;
            case 4:
                prefix = "T";
                break;
            case 5:
                prefix = "H";
                break;
            case 6:
                prefix = "G";
                break;
            default:
                prefix = "X";
        }
        
        // Find the highest number for this prefix
        List<Product> allProducts = inventory.getAllProducts();
        int maxNum = 0;
        for (Product p : allProducts) {
            String id = p.getProductID();
            if (id.startsWith(prefix + "-")) {
                try {
                    String numStr = id.substring(prefix.length() + 1);
                    int num = Integer.parseInt(numStr);
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore malformed IDs
                }
            }
        }
        
        return String.format("%s-%03d", prefix, maxNum + 1);
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
     * Handles restocking an existing product with validation.
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
        
        int amountToAdd = -1;
        boolean validInput = false;
        
        // Keep asking until valid input
        while (!validInput) {
            try {
                String input = ConsoleHelper.getStringInput("Enter amount to add (must be a whole number > 0): ");
                amountToAdd = Integer.parseInt(input);
                
                if (amountToAdd <= 0) {
                    System.out.println("Error: Amount must be greater than 0. Please try again.");
                } else {
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a whole number greater than 0.");
            }
        }
        
        product.setQuantityInStock(product.getQuantityInStock() + amountToAdd);
        dataHandler.saveInventory(inventory);
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
    private void handleViewSalesLog() {
        System.out.println("\n--- Sales Transaction Log ---");
        dataHandler.displaySalesLog();
    }

    /**
     * Handles updating product information (name, brand, variant).
     */
    private void handleUpdateProductInfo() {
        System.out.println("\n--- Update Product Information ---");
        String productID = ConsoleHelper.getStringInput("Enter Product ID: ");
        
        Product product = inventory.findProductByID(productID);
        
        if (product == null) {
            System.out.println("Error: Product not found.");
            return;
        }
        
        System.out.println("Current Info:");
        System.out.println("Name: " + product.getName());
        System.out.println("Brand: " + product.getBrand());
        System.out.println("Variant: " + product.getVariant());
        
        boolean updateName = ConsoleHelper.getYesNoInput("Update name?");
        if (updateName) {
            String newName = ConsoleHelper.getStringInput("Enter new name: ");
            product.setName(newName);
        }
        
        boolean updateBrand = ConsoleHelper.getYesNoInput("Update brand?");
        if (updateBrand) {
            String newBrand = ConsoleHelper.getStringInput("Enter new brand: ");
            product.setBrand(newBrand);
        }
        
        boolean updateVariant = ConsoleHelper.getYesNoInput("Update variant?");
        if (updateVariant) {
            String newVariant = ConsoleHelper.getStringInput("Enter new variant: ");
            product.setVariant(newVariant);
        }
        
        dataHandler.saveInventory(inventory);
        System.out.println("Product information updated and saved successfully!");
    }

    /**
     * Handles updating product price.
     */
    private void handleUpdateProductPrice() {
        System.out.println("\n--- Update Product Price ---");
        String productID = ConsoleHelper.getStringInput("Enter Product ID: ");
        
        Product product = inventory.findProductByID(productID);
        
        if (product == null) {
            System.out.println("Error: Product not found.");
            return;
        }
        
        System.out.println("Product: " + product.getName());
        System.out.println("Current Price: ₱" + String.format("%.2f", product.getPrice()));
        
        double newPrice = ConsoleHelper.getDoubleInput("Enter new price: ", 0.01);
        product.setPrice(newPrice);
        
        dataHandler.saveInventory(inventory);
        System.out.println("Price updated and saved successfully!");
        System.out.println("New Price: ₱" + String.format("%.2f", product.getPrice()));
    }
}

