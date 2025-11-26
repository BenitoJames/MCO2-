package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import model.*;

/**
 * Handles all file I/O for saving and loading data.
 */
public class StoreDataHandler {
    private static final String INVENTORY_FILE = "inventory.csv";
    private static final String CUSTOMERS_FILE = "customers.csv";
    private static final String SALES_LOG_FILE = "sales_log.txt";
    private static final String RECEIPT_LOG_FILE = "receipts.log";
    
    /**
     * Constructs a StoreDataHandler.
     */
    public StoreDataHandler() {
        // Ensure files exist (or create them)
        try {
            new FileWriter(INVENTORY_FILE, true).close();
            new FileWriter(CUSTOMERS_FILE, true).close();
            new FileWriter(SALES_LOG_FILE, true).close();
            new FileWriter(RECEIPT_LOG_FILE, true).close();
        } catch (IOException e) {
            System.err.println("Error initializing data files: " + e.getMessage());
        }
    }
    
    /**
     * Saves the current inventory to INVENTORY_FILE.
     *
     * @param inventory (Inventory) The inventory object to save.
     */
    public void saveInventory(Inventory inventory) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(INVENTORY_FILE)))) {
            List<Product> allProducts = inventory.getAllProducts();
            for (Product p : allProducts) {
                StringJoiner csvLine = new StringJoiner(",");
                String type = p.getClass().getSimpleName();
                csvLine.add(type);
                csvLine.add(p.getProductID());
                csvLine.add(p.getName());
                csvLine.add(String.valueOf(p.getPrice()));
                csvLine.add(String.valueOf(p.getQuantityInStock()));
                csvLine.add(String.join(";", p.getSubcategories()));
                csvLine.add(p.getBrand());
                csvLine.add(p.getVariant());
                
                if (p instanceof PerishableProduct) {
                    csvLine.add(((PerishableProduct) p).getExpirationDate().toString());
                } else {
                    csvLine.add("N/A");
                }
                
                out.println(csvLine.toString());
            }
            
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }
    
    /**
     * Loads the inventory from INVENTORY_FILE.
     * If the file is empty, it creates a default inventory.
     *
     * @return (Inventory) The loaded inventory.
     */
    public Inventory loadInventory() {
        Inventory inventory = new Inventory();
        boolean isEmpty = true;
        
        try (BufferedReader br = new BufferedReader(new FileReader(INVENTORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Skip empty lines
                isEmpty = false;
                
                String[] data = line.split(",");
                try {
                    String type = data[0];
                    String id = data[1];
                    String name = data[2];
                    double price = Double.parseDouble(data[3]);
                    int stock = Integer.parseInt(data[4]);
                    List<String> subcats = new ArrayList<>(Arrays.asList(data[5].split(";")));
                    String brand = data[6];
                    String variant = data[7];
                    String expiryStr = data[8];
                    
                    Product p = null;
                    
                    if (!expiryStr.equals("N/A")) {
                        LocalDate expiry = LocalDate.parse(expiryStr);
                        
                        switch (type) {
                            case "Food":
                                p = new Food(id, name, price, stock, subcats, brand, variant, expiry);
                                break;
                            case "Beverages":
                                p = new Beverages(id, name, price, stock, subcats, brand, variant, expiry);
                                break;
                            case "Pharmacy":
                                p = new Pharmacy(id, name, price, stock, subcats, brand, variant, expiry);
                                break;
                        }
                        
                    } else {
                        switch (type) {
                            case "Toiletries":
                                p = new Toiletries(id, name, price, stock, subcats, brand, variant);
                                break;
                            case "HouseholdAndPet":
                                p = new HouseholdAndPet(id, name, price, stock, subcats, brand, variant);
                                break;
                            case "GeneralAndSpecialty":
                                p = new GeneralAndSpecialty(id, name, price, stock, subcats, brand, variant);
                                break;
                        }
                    }
                    
                    if (p != null) {
                        inventory.addProduct(p);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error parsing inventory line (skipping): " + line);
                    e.printStackTrace();
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error loading inventory: " + e.getMessage());
        }
        
        if (isEmpty) {
            System.out.println("Inventory file is empty. Loading default items.");
            inventory = createDefaultInventory();
            saveInventory(inventory);
        }
        
        return inventory;
    }
    
    /**
     * Creates a default inventory set with proper quantities per spec.
     *
     * @return (Inventory) A new inventory with default items.
     */
    private Inventory createDefaultInventory() {
        Inventory inventory = new Inventory();
        
        // FOOD - 5+ items, qty 10 each
        inventory.addProduct(new Food("F-001", "Chicken Sandwich", 150.00, 10,
            Arrays.asList("Food", "Ready-to-eat", "Sandwich"), "7-Eleven", "Regular", LocalDate.now().plusDays(3)));
        inventory.addProduct(new Food("F-002", "Tuna Sandwich", 140.00, 10,
            Arrays.asList("Food", "Ready-to-eat", "Sandwich"), "7-Eleven", "Regular", LocalDate.now().plusDays(3)));
        inventory.addProduct(new Food("F-003", "Fried Chicken", 80.00, 10,
            Arrays.asList("Food", "Ready-to-eat", "Hot Meal"), "Family Mart", "2pc", LocalDate.now().plusDays(1)));
        inventory.addProduct(new Food("F-004", "Siopao", 35.00, 10,
            Arrays.asList("Food", "Ready-to-eat", "Snack"), "Pork Bun", "Asado", LocalDate.now().plusDays(2)));
        inventory.addProduct(new Food("F-005", "Cup Noodles", 30.00, 10,
            Arrays.asList("Food", "Instant", "Noodles"), "Nissin", "Beef", LocalDate.now().plusDays(180)));
        
        // BEVERAGES - 5+ items, qty 10 each
        inventory.addProduct(new Beverages("B-001", "Coke", 50.00, 10,
            Arrays.asList("Beverages", "Soft Drinks", "Cold"), "Coca-Cola", "Regular", LocalDate.now().plusDays(90)));
        inventory.addProduct(new Beverages("B-002", "Sprite", 50.00, 10,
            Arrays.asList("Beverages", "Soft Drinks", "Cold"), "Sprite", "Regular", LocalDate.now().plusDays(90)));
        inventory.addProduct(new Beverages("B-003", "Bottled Water", 20.00, 10,
            Arrays.asList("Beverages", "Water", "Cold"), "Nature's Spring", "500ml", LocalDate.now().plusDays(365)));
        inventory.addProduct(new Beverages("B-004", "Energy Drink", 60.00, 10,
            Arrays.asList("Beverages", "Energy", "Cold"), "Red Bull", "250ml", LocalDate.now().plusDays(180)));
        inventory.addProduct(new Beverages("B-005", "Iced Coffee", 70.00, 10,
            Arrays.asList("Beverages", "Coffee", "Cold"), "Nescafe", "Latte", LocalDate.now().plusDays(30)));
        
        // PHARMACY - 5+ items, qty 10 each
        inventory.addProduct(new Pharmacy("P-001", "Paracetamol", 10.00, 10,
            Arrays.asList("Pharmacy", "Medicine", "Pain Relief"), "Biogesic", "500mg", LocalDate.now().plusDays(365)));
        inventory.addProduct(new Pharmacy("P-002", "Ibuprofen", 15.00, 10,
            Arrays.asList("Pharmacy", "Medicine", "Pain Relief"), "Advil", "200mg", LocalDate.now().plusDays(365)));
        inventory.addProduct(new Pharmacy("P-003", "Antihistamine", 20.00, 10,
            Arrays.asList("Pharmacy", "Medicine", "Allergy"), "Benadryl", "25mg", LocalDate.now().plusDays(365)));
        inventory.addProduct(new Pharmacy("P-004", "Cough Syrup", 80.00, 10,
            Arrays.asList("Pharmacy", "Medicine", "Cough"), "Robitussin", "120ml", LocalDate.now().plusDays(180)));
        inventory.addProduct(new Pharmacy("P-005", "Vitamin C", 25.00, 10,
            Arrays.asList("Pharmacy", "Vitamins", "Supplement"), "Ceelin", "100mg", LocalDate.now().plusDays(365)));
        
        // TOILETRIES - 5+ items, qty 10 each
        inventory.addProduct(new Toiletries("T-001", "Shampoo", 120.00, 10,
            Arrays.asList("Toiletries", "Hair Care"), "Dove", "Nourishing"));
        inventory.addProduct(new Toiletries("T-002", "Soap", 40.00, 10,
            Arrays.asList("Toiletries", "Body Care"), "Safeguard", "Classic"));
        inventory.addProduct(new Toiletries("T-003", "Toothpaste", 60.00, 10,
            Arrays.asList("Toiletries", "Oral Care"), "Colgate", "Total"));
        inventory.addProduct(new Toiletries("T-004", "Deodorant", 80.00, 10,
            Arrays.asList("Toiletries", "Body Care"), "Rexona", "Ice Cool"));
        inventory.addProduct(new Toiletries("T-005", "Facial Wash", 150.00, 10,
            Arrays.asList("Toiletries", "Skin Care"), "Cetaphil", "Gentle"));
        
        // HOUSEHOLD - 5+ items, qty 10 each
        inventory.addProduct(new HouseholdAndPet("H-001", "Dishwashing Liquid", 80.00, 10,
            Arrays.asList("Household", "Cleaning"), "Joy", "Lemon"));
        inventory.addProduct(new HouseholdAndPet("H-002", "Laundry Detergent", 150.00, 10,
            Arrays.asList("Household", "Cleaning"), "Tide", "Original"));
        inventory.addProduct(new HouseholdAndPet("H-003", "Tissue Paper", 50.00, 10,
            Arrays.asList("Household", "Paper"), "Kleenex", "Soft"));
        inventory.addProduct(new HouseholdAndPet("H-004", "Hand Sanitizer", 90.00, 10,
            Arrays.asList("Household", "Hygiene"), "Purell", "Original"));
        inventory.addProduct(new HouseholdAndPet("H-005", "Air Freshener", 120.00, 10,
            Arrays.asList("Household", "Air Care"), "Glade", "Lavender"));
        
        // GENERAL - 5+ items, qty 10 each
        inventory.addProduct(new GeneralAndSpecialty("G-001", "Cigarettes", 150.00, 10,
            Arrays.asList("General", "Tobacco"), "Marlboro", "Red"));
        inventory.addProduct(new GeneralAndSpecialty("G-002", "Lighter", 15.00, 10,
            Arrays.asList("General", "Accessories"), "BIC", "Regular"));
        inventory.addProduct(new GeneralAndSpecialty("G-003", "Phone Load", 100.00, 10,
            Arrays.asList("Specialty", "Mobile"), "Smart", "Regular"));
        inventory.addProduct(new GeneralAndSpecialty("G-004", "Batteries", 60.00, 10,
            Arrays.asList("General", "Electronics"), "Eveready", "AA"));
        inventory.addProduct(new GeneralAndSpecialty("G-005", "Notebook", 40.00, 10,
            Arrays.asList("General", "School Supplies"), "Mongol", "Spiral"));
        
        return inventory;
    }
    
    /**
     * Saves the customer list to CUSTOMERS_FILE.
     * Format: UserID,LastName,FirstName,MiddleName,Password,MembershipCardID,CardExpiry,Points
     *
     * @param customerList (List<Customer>) The list of customers.
     */
    public void saveCustomers(List<Customer> customerList) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(CUSTOMERS_FILE)))) {
            for (Customer c : customerList) {
                if (c.isGuest()) continue; // Don't save guest users
                
                StringJoiner csvLine = new StringJoiner(",");
                csvLine.add(c.getUserID());
                csvLine.add(c.getLastName());
                csvLine.add(c.getFirstName());
                csvLine.add(c.getMiddleName() != null && !c.getMiddleName().isEmpty() ? c.getMiddleName() : "");
                csvLine.add(c.getPassword());
                csvLine.add(c.getMembershipCardID() != null ? c.getMembershipCardID() : "");
                csvLine.add(c.getCardExpiryDate() != null ? c.getCardExpiryDate().toString() : "");
                csvLine.add(String.valueOf(c.getPoints()));
                
                out.println(csvLine.toString());
            }
            
        } catch (IOException e) {
            System.err.println("Error saving customers: " + e.getMessage());
        }
    }
    
    /**
     * Loads the customer list from CUSTOMERS_FILE.
     * Format: UserID,LastName,FirstName,MiddleName,Password,MembershipCardID,CardExpiry,Points
     *
     * @return (List<Customer>) The loaded customer list.
     */
    public List<Customer> loadCustomers() {
        List<Customer> customerList = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(CUSTOMERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",", -1); // -1 to keep empty strings
                try {
                    // Handle both old and new formats
                    if (data.length >= 8) {
                        // New format
                        String userID = data[0];
                        String lastName = data[1];
                        String firstName = data[2];
                        String middleName = data[3];
                        String password = data[4];
                        String cardID = data[5];
                        String expiryStr = data[6];
                        int points = Integer.parseInt(data[7]);
                        
                        Customer customer = new Customer(userID, lastName, firstName, middleName, password);
                        
                        if (!cardID.isEmpty() && !expiryStr.isEmpty()) {
                            LocalDate expiry = LocalDate.parse(expiryStr);
                            customer.assignMembershipCard(cardID, expiry);
                        }
                        
                        customer.setPoints(points);
                        customerList.add(customer);
                    } else if (data.length >= 2) {
                        // Old format migration: C-001,Full Name,isSenior,points
                        // Convert to new format with default values
                        String oldID = data[0];
                        String fullName = data[1];
                        
                        // Parse name (assume "Last First" or just "Name")
                        String[] nameParts = fullName.split(" ", 2);
                        String lastName = nameParts[0];
                        String firstName = nameParts.length > 1 ? nameParts[1] : "";
                        
                        // Generate new User ID
                        String newUserID = generateUserID(customerList);
                        
                        // Default password for migrated accounts
                        String defaultPassword = "password123";
                        
                        Customer customer = new Customer(newUserID, lastName, firstName, "", defaultPassword);
                        
                        // Check for membership points (old format)
                        if (data.length >= 4 && !data[3].equals("N/A")) {
                            int points = Integer.parseInt(data[3]);
                            customer.setPoints(points);
                        }
                        
                        customerList.add(customer);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error parsing customer line (skipping): " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
        
        return customerList;
    }
    
    /**
     * Generates a new User ID in format DLSUser-XXX.
     *
     * @param existingCustomers List of existing customers to check for ID conflicts.
     * @return A unique User ID.
     */
    public String generateUserID(List<Customer> existingCustomers) {
        int maxNum = 0;
        
        for (Customer c : existingCustomers) {
            String id = c.getUserID();
            if (id != null && id.startsWith("DLSUser-")) {
                try {
                    int num = Integer.parseInt(id.substring(8));
                    maxNum = Math.max(maxNum, num);
                } catch (NumberFormatException e) {
                    // Ignore malformed IDs
                }
            }
        }
        
        return String.format("DLSUser-%03d", maxNum + 1);
    }
    
    /**
     * Generates a new Membership Card ID in format DLSUCS-XXXXXXXX.
     *
     * @param existingCustomers List of existing customers to check for ID conflicts.
     * @return A unique Membership Card ID.
     */
    public String generateMembershipCardID(List<Customer> existingCustomers) {
        int maxNum = 0;
        
        for (Customer c : existingCustomers) {
            String cardID = c.getMembershipCardID();
            if (cardID != null && cardID.startsWith("DLSUCS-")) {
                try {
                    int num = Integer.parseInt(cardID.substring(7));
                    maxNum = Math.max(maxNum, num);
                } catch (NumberFormatException e) {
                    // Ignore malformed IDs
                }
            }
        }
        
        return String.format("DLSUCS-%08d", maxNum + 1);
    }
    
    /**
     * Saves a receipt string to RECEIPT_LOG_FILE.
     *
     * @param receipt (String) The receipt text.
     */
    public void saveReceipt(String receipt) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(RECEIPT_LOG_FILE, true)))) {
            out.println(receipt);
            out.println("========================================\n");
        } catch (IOException e) {
            System.err.println("Error saving receipt: " + e.getMessage());
        }
    }
    
    /**
     * Saves a transaction summary to SALES_LOG_FILE.
     *
     * @param summary (String) The CSV summary line.
     */
    public void saveSalesLog(String summary) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(SALES_LOG_FILE, true)))) {
            out.println(summary);
        } catch (IOException e) {
            System.err.println("Error saving sales log: " + e.getMessage());
        }
    }
    
    /**
     * Displays the sales log to the console.
     */
    public void displaySalesLog() {
        try (BufferedReader br = new BufferedReader(new FileReader(SALES_LOG_FILE))) {
            String line;
            boolean hasData = false;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                hasData = true;
            }
            if (!hasData) {
                System.out.println("No sales transactions recorded.");
            }
        } catch (IOException e) {
            System.err.println("Error reading sales log: " + e.getMessage());
        }
    }
}