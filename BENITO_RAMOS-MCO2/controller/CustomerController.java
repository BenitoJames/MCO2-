package controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.*;
import util.*;

/**
 * Handles all logic for the customer-facing shopping experience.
 * Implements "what if" features:
 * - Live stock reduction on add to cart.
 * - Stock refund on remove from cart or leaving.
 * - Saving receipts to a log file.
 */
public class CustomerController {

    private Inventory inventory;
    private Customer currentCustomer;
    private List<CartItem> shoppingCart;
    private StoreDataHandler dataHandler;
    private List<Customer> customerList;
    private boolean isShopping;
    private boolean checkoutComplete;

    /**
     * Constructs a CustomerController.
     *
     * @param inventory (Inventory) A reference to the main inventory.
     * @param customer (Customer) The customer who is shopping.
     * @param dataHandler (StoreDataHandler) A reference to the data handler for saving receipts.
     * @param customerList (List<Customer>) A reference to the customer list.
     */
    public CustomerController(Inventory inventory, Customer customer, StoreDataHandler dataHandler, List<Customer> customerList) {
        this.inventory = inventory;
        this.currentCustomer = customer;
        this.dataHandler = dataHandler;
        this.customerList = customerList;
        this.shoppingCart = new ArrayList<>();
        this.isShopping = true;
        this.checkoutComplete = false; // Flag to check if checkout was successful
    }

    /**
     * Runs the main loop for the customer shopping menu.
     * Ensures stock is refunded if the customer leaves without completing checkout.
     */
    public void run() {
        while (this.isShopping) {
            showCustomerMenu();
            int choice = ConsoleHelper.getIntInput("Enter your choice: ", 0, 4);
            switch (choice) {
                case 1:
                    handleBrowseInventory();
                    break;
                case 2:
                    handleAddItemToCart();
                    break;
                case 3:
                    handleRemoveFromCart();
                    break;
                case 4:
                    handleCheckout();
                    break;
                case 0:
                    this.isShopping = false;
                    break;
            }
        }
        
        // Cleanup: ensure stock is refunded if checkout was not completed
        cleanupOnExit();
    }

    /**
     * Cleans up when the customer exits the shopping session.
     * Refunds all cart stock if checkout was not completed.
     */
    private void cleanupOnExit() {
        if (!checkoutComplete) {
            // If the user did not complete checkout, refund all items
            refundAllCartStock();
            
            if (currentCustomer.hasMembership()) {
                System.out.println("Returning to main menu. Your cart has been emptied.");
            }
        }
    }

    /**
     * Displays the customer menu options.
     */
    private void showCustomerMenu() {
        System.out.println("\n--- Customer Menu ---");
        System.out.println("Welcome, " + currentCustomer.getName() + "!");
        System.out.println("1. Browse Inventory");
        System.out.println("2. Add Item to Cart");
        System.out.println("3. Remove Item from Cart");
        System.out.println("4. View Cart & Checkout");
        System.out.println("0. Cancel and Return to Main Menu");
    }

    /**
     * Displays the entire inventory with category filtering.
     */
    private void handleBrowseInventory() {
        while (true) {
            System.out.println("\n--- Browse Products by Category ---");
            System.out.println("1. All Products");
            System.out.println("2. Food");
            System.out.println("3. Beverages");
            System.out.println("4. Toiletries");
            System.out.println("5. Household and Pet");
            System.out.println("6. Pharmacy");
            System.out.println("7. General and Specialty");
            System.out.println("8. Search by Name");
            System.out.println("0. Back to Customer Menu");
            
            int choice = ConsoleHelper.getIntInput("Select option: ", 0, 8);
            
            if (choice == 0) {
                break;
            } else if (choice == 8) {
                handleSearchByName();
            } else {
                String category = getCategoryName(choice);
                displayProductsByCategory(category, choice);
            }
        }
    }
    
    /**
     * Gets the category name based on choice.
     */
    private String getCategoryName(int choice) {
        switch (choice) {
            case 1: return "All";
            case 2: return "F";
            case 3: return "B";
            case 4: return "T";
            case 5: return "H";
            case 6: return "P";
            case 7: return "G";
            default: return "All";
        }
    }
    
    /**
     * Displays products filtered by category.
     */
    private void displayProductsByCategory(String categoryPrefix, int choice) {
        System.out.println("\n--- " + getCategoryDisplayName(choice) + " ---");
        List<Product> allProducts = inventory.getAllProducts();
        boolean found = false;
        
        for (Product product : allProducts) {
            if (product.getQuantityInStock() > 0) {
                if (categoryPrefix.equals("All") || matchesCategory(product.getProductID(), categoryPrefix)) {
                    System.out.println(product.displayDetails());
                    found = true;
                }
            }
        }
        
        if (!found) {
            System.out.println("No products available in this category.");
        }
        
        ConsoleHelper.getStringInput("\nPress Enter to continue...");
    }
    
    /**
     * Gets display name for category.
     */
    private String getCategoryDisplayName(int choice) {
        switch (choice) {
            case 1: return "All Products";
            case 2: return "Food";
            case 3: return "Beverages";
            case 4: return "Toiletries";
            case 5: return "Household and Pet";
            case 6: return "Pharmacy";
            case 7: return "General and Specialty";
            default: return "All Products";
        }
    }
    
    /**
     * Checks if product ID matches category prefix.
     */
    private boolean matchesCategory(String productID, String prefix) {
        if (productID == null) return false;
        return productID.toUpperCase().startsWith(prefix.toUpperCase());
    }

    /**
     * Handles searching products by name.
     */
    private void handleSearchByName() {
        System.out.println("\n--- Search Products by Name ---");
        String searchQuery = ConsoleHelper.getStringInput("Enter product name to search: ");
        
        if (searchQuery.trim().isEmpty()) {
            System.out.println("Search query cannot be empty.");
            return;
        }
        
        List<Product> allProducts = inventory.getAllProducts();
        List<Product> matchingProducts = new ArrayList<>();
        
        String lowerQuery = searchQuery.toLowerCase();
        
        for (Product product : allProducts) {
            if (product.getQuantityInStock() > 0) {
                if (product.getName().toLowerCase().contains(lowerQuery)) {
                    matchingProducts.add(product);
                }
            }
        }
        
        if (matchingProducts.isEmpty()) {
            System.out.println("No products found matching: " + searchQuery);
        } else {
            System.out.println("\n--- Search Results (" + matchingProducts.size() + " found) ---");
            for (Product product : matchingProducts) {
                System.out.println(product.displayDetails());
            }
        }
        
        ConsoleHelper.getStringInput("\nPress Enter to continue...");
    }

    /**
     * Handles adding an item to the shopping cart.
     * Implements "what if": live stock reduction.
     */
    private void handleAddItemToCart() {
        System.out.println("\n--- Add Item to Cart ---");
        System.out.println("1. Enter Product ID");
        System.out.println("2. Search by Name");
        System.out.println("0. Back to Customer Menu");
        
        int choice = ConsoleHelper.getIntInput("Select option: ", 0, 2);
        
        if (choice == 0) {
            return;
        }
        
        String productID = null;
        
        if (choice == 1) {
            productID = ConsoleHelper.getStringInput("Enter Product ID to add: ");
        } else if (choice == 2) {
            String searchQuery = ConsoleHelper.getStringInput("Enter product name to search: ");
            
            if (searchQuery.trim().isEmpty()) {
                System.out.println("Search query cannot be empty.");
                return;
            }
            
            List<Product> matchingProducts = searchProductsByName(searchQuery);
            
            if (matchingProducts.isEmpty()) {
                System.out.println("No products found matching: " + searchQuery);
                return;
            }
            
            System.out.println("\n--- Search Results ---");
            for (int i = 0; i < matchingProducts.size(); i++) {
                System.out.println((i + 1) + ". " + matchingProducts.get(i).displayDetails());
            }
            
            if (matchingProducts.size() == 1) {
                productID = matchingProducts.get(0).getProductID();
                System.out.println("Selected: " + matchingProducts.get(0).getName());
            } else {
                int selection = ConsoleHelper.getIntInput("Select product number (0 to cancel): ", 0, matchingProducts.size());
                if (selection == 0) {
                    return;
                }
                productID = matchingProducts.get(selection - 1).getProductID();
            }
        }
        
        Product product = inventory.findProductByID(productID);
        
        if (product == null) {
            System.out.println("Error: Product not found.");
            return;
        }

        if (product.getQuantityInStock() <= 0) {
            System.out.println("Sorry, " + product.getName() + " is out of stock.");
            return;
        }

        System.out.println("Product: " + product.getName());
        System.out.println("Stock: " + product.getQuantityInStock());
        int quantity = ConsoleHelper.getIntInput("Enter quantity: ", 1, product.getQuantityInStock());

        // Reduce stock immediately
        product.setQuantityInStock(product.getQuantityInStock() - quantity);
        
        // Add to cart
        // Check if item is already in cart
        CartItem existingItem = null;
        boolean found = false;
        int i = 0;
        while(i < shoppingCart.size() && !found) {
            if (shoppingCart.get(i).getProduct().getProductID().equals(productID)) {
                existingItem = shoppingCart.get(i);
                found = true;
            }
            i++;
        }

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            shoppingCart.add(new CartItem(product, quantity));
        }

        System.out.println("Added " + quantity + "x " + product.getName() + " to cart.");
        System.out.println("Remaining stock: " + product.getQuantityInStock());
    }

    /**
     * Searches for products by name (case-insensitive partial match).
     * @param query (String) The search query.
     * @return (List<Product>) List of matching products.
     */
    private List<Product> searchProductsByName(String query) {
        List<Product> allProducts = inventory.getAllProducts();
        List<Product> matchingProducts = new ArrayList<>();
        
        String lowerQuery = query.toLowerCase();
        
        for (Product product : allProducts) {
            if (product.getQuantityInStock() > 0) {
                if (product.getName().toLowerCase().contains(lowerQuery)) {
                    matchingProducts.add(product);
                }
            }
        }
        
        return matchingProducts;
    }

    /**
     * Handles removing an item from the shopping cart.
     * Implements "what if": stock refund.
     */
    private void handleRemoveFromCart() {
        System.out.println("\n--- Remove Item from Cart ---");
        if (shoppingCart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }

        // 1. Display cart
        System.out.println("Your current cart:");
        for (int i = 0; i < shoppingCart.size(); i++) {
            System.out.println((i + 1) + ". " + shoppingCart.get(i).toString());
        }

        // 2. Get item to remove
        int itemIndex = ConsoleHelper.getIntInput("Enter item number to remove: ", 1, shoppingCart.size()) - 1;
        CartItem itemToRemove = shoppingCart.get(itemIndex);
        Product product = itemToRemove.getProduct();
        
        System.out.println("Item: " + product.getName());
        System.out.println("Quantity in cart: " + itemToRemove.getQuantity());
        
        // 3. Get quantity to remove
        int quantityToRemove = ConsoleHelper.getIntInput("Enter quantity to remove: ", 1, itemToRemove.getQuantity());

        // 4. Refund stock
        product.setQuantityInStock(product.getQuantityInStock() + quantityToRemove);
        System.out.println("Refunded " + quantityToRemove + "x " + product.getName() + " to stock.");
        System.out.println("New stock: " + product.getQuantityInStock());

        // 5. Update cart
        if (quantityToRemove == itemToRemove.getQuantity()) {
            // Remove item entirely
            shoppingCart.remove(itemIndex);
            System.out.println("Item removed from cart.");
        } else {
            // Just decrease quantity
            itemToRemove.setQuantity(itemToRemove.getQuantity() - quantityToRemove);
            System.out.println("Cart updated. New quantity: " + itemToRemove.getQuantity());
        }
    }

    /**
     * Refunds all stock from the cart to the inventory.
     * Used when customer cancels or fails checkout.
     */
    private void refundAllCartStock() {
        for (CartItem item : shoppingCart) {
            Product product = item.getProduct();
            product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
        }
        shoppingCart.clear();
    }


    /**
     * Handles the checkout process.
     */
    private void handleCheckout() {
        System.out.println("\n--- View Cart & Checkout ---");
        if (shoppingCart.isEmpty()) {
            System.out.println("Your cart is empty. Nothing to check out.");
            return;
        }

        // Step 1: Senior/PWD Dialog (for EVERYONE)
        boolean isSenior = false;
        boolean askSenior = ConsoleHelper.getYesNoInput("Are you a Senior Citizen or PWD? (y/n): ");
        if (askSenior) {
            String seniorID = ConsoleHelper.getStringInput("Enter your Senior/PWD ID (format: SRC-XXXX or PWD-XXXX): ");
            if (isValidSeniorPWDID(seniorID)) {
                isSenior = true;
                System.out.println("Senior/PWD discount applied (20% off after VAT deduction).");
            } else {
                System.out.println("Invalid Senior/PWD ID format. Discount not applied.");
            }
        }

        // Step 2: Membership Card Dialog (only if user doesn't have one)
        double membershipFee = 0.0;
        if (!currentCustomer.hasMembership()) {
            if (currentCustomer.isGuest()) {
                // Guest user - show 3 options
                System.out.println("\nMembership Card Options:");
                System.out.println("1. Yes, purchase for ₱50.00");
                System.out.println("2. No, continue without membership");
                System.out.println("3. I have a card already");
                int memberChoice = ConsoleHelper.getIntInput("Enter choice: ", 1, 3);
                
                if (memberChoice == 1) {
                    // Purchase membership and create account for guest
                    membershipFee = 50.0;
                    String userID = generateUserID();
                    String password = "DLSUser2025";
                    String membershipID = generateMembershipID();
                    LocalDate expiryDate = LocalDate.now().plusYears(1);
                    
                    // Update current customer to be a registered user
                    currentCustomer = new Customer(userID, "Guest", "User", "", password);
                    currentCustomer.assignMembershipCard(membershipID, expiryDate);
                    customerList.add(currentCustomer);
                    
                    System.out.println("\n=== Account Created ===");
                    System.out.println("User ID: " + userID);
                    System.out.println("Password: " + password);
                    System.out.println("Membership ID: " + membershipID);
                    System.out.println("Valid until: " + expiryDate);
                    System.out.println("An account has been created for you. You can sign in next time!");
                    System.out.println("=======================\n");
                    
                } else if (memberChoice == 3) {
                    // Guest has existing card - ask for membership ID
                    String membershipID = ConsoleHelper.getStringInput("Enter your Membership Card ID (DLSUCS-XXXXXXXX): ");
                    
                    // Find customer with this membership ID
                    Customer foundCustomer = null;
                    for (Customer c : customerList) {
                        if (c.getMembershipCardID() != null && c.getMembershipCardID().equals(membershipID)) {
                            foundCustomer = c;
                            break;
                        }
                    }
                    
                    if (foundCustomer != null) {
                        currentCustomer = foundCustomer;
                        System.out.println("Welcome back, " + currentCustomer.getName() + "!");
                        System.out.println("Current points: " + currentCustomer.getPoints());
                    } else {
                        System.out.println("Membership card not found in system. Continuing as guest.");
                    }
                }
            } else {
                // Signed-in user without membership
                boolean wantsMembership = ConsoleHelper.getYesNoInput("Purchase a membership card for ₱50.00? (y/n): ");
                if (wantsMembership) {
                    membershipFee = 50.0;
                    String membershipID = generateMembershipID();
                    LocalDate expiryDate = LocalDate.now().plusYears(1);
                    currentCustomer.assignMembershipCard(membershipID, expiryDate);
                    
                    System.out.println("Membership card purchased!");
                    System.out.println("Membership ID: " + membershipID);
                    System.out.println("Valid until: " + expiryDate);
                }
            }
        }

        // Step 3: Calculate Order Summary
        double subtotal = 0.0;
        for (CartItem item : shoppingCart) {
            subtotal += item.getProduct().getPrice() * item.getQuantity();
        }
        
        double vat = subtotal * 0.12;
        double seniorDiscount = 0.0;
        
        if (isSenior) {
            // Senior/PWD: subtract VAT first, then apply 20% discount
            double amountAfterVAT = subtotal - vat;
            seniorDiscount = amountAfterVAT * 0.20;
        }
        
        double totalDue = subtotal - seniorDiscount + membershipFee;
        
        // Display Order Summary
        System.out.println("\n========== ORDER SUMMARY ==========");
        System.out.println("Subtotal:                ₱" + String.format("%.2f", subtotal));
        System.out.println("VAT (12%):               ₱" + String.format("%.2f", vat));
        if (isSenior) {
            System.out.println("Senior/PWD Discount:    -₱" + String.format("%.2f", seniorDiscount));
        }
        if (membershipFee > 0) {
            System.out.println("Membership Card:        +₱" + String.format("%.2f", membershipFee));
        }
        System.out.println("-----------------------------------");
        System.out.println("TOTAL DUE:               ₱" + String.format("%.2f", totalDue));
        System.out.println("===================================\n");
        
        // Show points information if member
        if (currentCustomer.hasMembership()) {
            int currentPoints = currentCustomer.getPoints();
            int pointsToEarn = (int) ((subtotal - seniorDiscount) / 50);
            System.out.println("Current Points: " + currentPoints);
            System.out.println("Points to be earned: " + pointsToEarn + " (1 point per ₱50)");
            
            // Point redemption
            if (currentPoints > 0) {
                boolean usePoints = ConsoleHelper.getYesNoInput("Use points to pay? (1 point = ₱1)");
                if (usePoints) {
                    int maxPoints = (int) Math.min(currentPoints, totalDue);
                    int pointsToUse = ConsoleHelper.getIntInput("Enter points to use (max " + maxPoints + "): ", 0, maxPoints);
                    
                    totalDue -= pointsToUse;
                    currentCustomer.usePoints(pointsToUse);
                    
                    System.out.println("Redeemed " + pointsToUse + " points.");
                    System.out.println(String.format("New Total Due: ₱%.2f", totalDue));
                }
            }
            System.out.println();
        }

        // Step 4: Payment
        System.out.println("\n=== PAYMENT ===");
        System.out.println("Total to pay: ₱" + String.format("%.2f", totalDue));
        System.out.println("\nSelect Payment Method:");
        System.out.println("1. Cash");
        System.out.println("2. Card");
        
        int paymentChoice = ConsoleHelper.getIntInput("Enter choice: ", 1, 2);
        
        String paymentMethod;
        double amountPaid;
        double change = 0.0;
        String cardDetails = "";
        
        if (paymentChoice == 1) {
            // Cash payment
            paymentMethod = "Cash";
            while (true) {
                amountPaid = ConsoleHelper.getDoubleInput("Enter amount to pay: ₱");
                
                if (amountPaid < totalDue) {
                    System.out.println("Insufficient payment! Total due: ₱" + String.format("%.2f", totalDue));
                    continue;
                }
                
                change = amountPaid - totalDue;
                System.out.println("\nPayment received: ₱" + String.format("%.2f", amountPaid));
                System.out.println("Change: ₱" + String.format("%.2f", change));
                break;
            }
        } else {
            // Card payment
            paymentMethod = "Card";
            cardDetails = handleCardPaymentConsole(totalDue);
            if (cardDetails == null) {
                System.out.println("Payment cancelled.");
                return;
            }
            amountPaid = totalDue;
        }
        
        // Calculate points to earn
        int pointsEarned = 0;
        if (currentCustomer.hasMembership()) {
            pointsEarned = (int) ((subtotal - seniorDiscount) / 50);
            currentCustomer.earnPoints(pointsEarned);
        }
        
        // Save customer data
        dataHandler.saveCustomers(customerList);
        
        // Show receipt
        printReceipt(paymentMethod, amountPaid, change, pointsEarned, cardDetails, subtotal, 
                     seniorDiscount, membershipFee, totalDue);
        
        // Mark as complete
        this.checkoutComplete = true;
        this.isShopping = false;
    }
    
    /**
     * Handles card payment in console with validation.
     * @param totalDue The total amount to charge
     * @return Card details string, or null if cancelled
     */
    private String handleCardPaymentConsole(double totalDue) {
        System.out.println("\n=== CARD PAYMENT ===");
        
        // Card number validation
        String cardNumber;
        while (true) {
            cardNumber = ConsoleHelper.getStringInput("Enter card number (16 digits): ");
            
            if (!cardNumber.matches("\\d{16}")) {
                System.out.println("Invalid card number! Must be 16 digits.");
                continue;
            }
            
            // Check if Visa or Mastercard
            if (!cardNumber.startsWith("4") && 
                !(cardNumber.startsWith("51") || cardNumber.startsWith("52") || 
                  cardNumber.startsWith("53") || cardNumber.startsWith("54") || 
                  cardNumber.startsWith("55"))) {
                System.out.println("Card not accepted! Only Visa or Mastercard.");
                continue;
            }
            
            break;
        }
        
        // CVV validation
        String cvv;
        while (true) {
            cvv = ConsoleHelper.getStringInput("Enter CVV (3 digits): ");
            
            if (!cvv.matches("\\d{3}")) {
                System.out.println("Invalid CVV! Must be 3 digits.");
                continue;
            }
            
            break;
        }
        
        // Expiry validation
        String expiry;
        while (true) {
            expiry = ConsoleHelper.getStringInput("Enter expiry date (MM/YY): ");
            
            if (!expiry.matches("\\d{2}/\\d{2}")) {
                System.out.println("Invalid format! Use MM/YY.");
                continue;
            }
            
            String[] parts = expiry.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = 2000 + Integer.parseInt(parts[1]);
            
            if (month < 1 || month > 12) {
                System.out.println("Invalid month! Must be 01-12.");
                continue;
            }
            
            // Check if expired
            java.time.YearMonth cardExpiry = java.time.YearMonth.of(year, month);
            java.time.YearMonth now = java.time.YearMonth.now();
            if (cardExpiry.isBefore(now)) {
                System.out.println("Card expired! Expiry date: " + expiry);
                continue;
            }
            
            break;
        }
        
        // Show confirmation
        String cardType = cardNumber.startsWith("4") ? "Visa" : "Mastercard";
        String maskedCard = "**** **** **** " + cardNumber.substring(12);
        
        System.out.println("\n=== CONFIRM CARD PAYMENT ===");
        System.out.println("Card Type: " + cardType);
        System.out.println("Card Number: " + maskedCard);
        System.out.println("Expiry: " + expiry);
        System.out.println("Amount to charge: ₱" + String.format("%.2f", totalDue));
        
        boolean confirm = ConsoleHelper.getYesNoInput("\nProceed with payment? (y/n): ");
        
        if (!confirm) {
            return null;
        }
        
        System.out.println("\nPayment successful!");
        return cardType + " " + maskedCard;
    }
    
    /**
     * Prints the receipt to console.
     */
    private void printReceipt(String paymentMethod, double amountPaid, double change,
                             int pointsEarned, String cardDetails, double subtotal,
                             double seniorDiscount, double membershipFee, double totalDue) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     DLSU CONVENIENCE STORE RECEIPT    ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();
        
        // Date and time
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("Date: " + now.format(formatter));
        System.out.println("Customer: " + currentCustomer.getName());
        if (currentCustomer.hasMembership()) {
            System.out.println("Member ID: " + currentCustomer.getMembershipCardID());
        }
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("ITEMS PURCHASED");
        System.out.println("----------------------------------------");
        
        // Print cart items
        for (CartItem item : shoppingCart) {
            System.out.println(String.format("%-20s x%d  ₱%.2f", 
                item.getProduct().getName(), 
                item.getQuantity(),
                item.getProduct().getPrice() * item.getQuantity()));
        }
        
        System.out.println("----------------------------------------");
        System.out.println(String.format("Subtotal:                ₱%.2f", subtotal));
        System.out.println(String.format("VAT (12%%):               ₱%.2f", subtotal * 0.12));
        
        if (seniorDiscount > 0) {
            System.out.println(String.format("Senior/PWD Discount:    -₱%.2f", seniorDiscount));
        }
        
        if (membershipFee > 0) {
            System.out.println(String.format("Membership Card:        +₱%.2f", membershipFee));
        }
        
        System.out.println("----------------------------------------");
        System.out.println(String.format("TOTAL DUE:               ₱%.2f", totalDue));
        System.out.println("----------------------------------------");
        System.out.println();
        
        System.out.println("PAYMENT DETAILS");
        System.out.println("Payment Method: " + paymentMethod);
        
        if ("Cash".equals(paymentMethod)) {
            System.out.println(String.format("Amount Paid:             ₱%.2f", amountPaid));
            System.out.println(String.format("Change:                  ₱%.2f", change));
        } else {
            System.out.println("Card: " + cardDetails);
            System.out.println(String.format("Amount Charged:          ₱%.2f", amountPaid));
        }
        
        if (currentCustomer.hasMembership()) {
            System.out.println();
            System.out.println("----------------------------------------");
            System.out.println("MEMBERSHIP POINTS");
            System.out.println("----------------------------------------");
            System.out.println(String.format("Points Earned: +%d", pointsEarned));
            System.out.println(String.format("Total Points: %d", currentCustomer.getPoints()));
        }
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("   Thank you for shopping with us!");
        System.out.println("       Please come again soon!");
        System.out.println("========================================");
        System.out.println();
    }

    /**
     * Validates Senior/PWD ID format.
     * @param id (String) The ID to validate.
     * @return (boolean) True if valid, false otherwise.
     */
    private boolean isValidSeniorPWDID(String id) {
        if (id == null) return false;
        return id.matches("^(SRC|PWD)-\\d{4}$");
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
     * Generates a membership card ID in the format DLSUCS-XXXXXXXX.
     * @return (String) The generated membership ID.
     */
    private String generateMembershipID() {
        return "DLSUCS-" + String.format("%08d", (int) (Math.random() * 100000000));
    }

    /**
     * Validates card payment details.
     * @return (boolean) True if card is valid, false otherwise.
     */
    private boolean validateCardPayment() {
        System.out.println("\n--- Card Payment Validation ---");
        
        // Card number
        String cardNumber = ConsoleHelper.getStringInput("Enter card number (16 digits): ");
        if (!isValidCardNumber(cardNumber)) {
            System.out.println("Invalid card number. Must be 16 digits (Visa or Mastercard).");
            return false;
        }
        
        // CVV
        String cvv = ConsoleHelper.getStringInput("Enter CVV (3 digits): ");
        if (!isValidCVV(cvv)) {
            System.out.println("Invalid CVV. Must be 3 digits.");
            return false;
        }
        
        // Expiry date
        String expiry = ConsoleHelper.getStringInput("Enter expiry date (MM/YY): ");
        if (!isValidExpiryDate(expiry)) {
            System.out.println("Invalid or expired date. Must be MM/YY format and in the future.");
            return false;
        }
        
        System.out.println("Card validated successfully.");
        return true;
    }

    /**
     * Validates card number format and type.
     * @param cardNumber (String) The card number to validate.
     * @return (boolean) True if valid, false otherwise.
     */
    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            return false;
        }
        // Check Visa (starts with 4) or Mastercard (starts with 51-55)
        return cardNumber.startsWith("4") || 
               (cardNumber.startsWith("51") || cardNumber.startsWith("52") || 
                cardNumber.startsWith("53") || cardNumber.startsWith("54") || 
                cardNumber.startsWith("55"));
    }

    /**
     * Validates CVV format.
     * @param cvv (String) The CVV to validate.
     * @return (boolean) True if valid, false otherwise.
     */
    private boolean isValidCVV(String cvv) {
        return cvv != null && cvv.matches("\\d{3}");
    }

    /**
     * Validates expiry date format and checks if it's in the future.
     * @param expiry (String) The expiry date in MM/YY format.
     * @return (boolean) True if valid and future date, false otherwise.
     */
    private boolean isValidExpiryDate(String expiry) {
        if (expiry == null || !expiry.matches("\\d{2}/\\d{2}")) {
            return false;
        }
        
        try {
            String[] parts = expiry.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = 2000 + Integer.parseInt(parts[1]);
            
            if (month < 1 || month > 12) {
                return false;
            }
            
            java.time.YearMonth expiryDate = java.time.YearMonth.of(year, month);
            java.time.YearMonth currentDate = java.time.YearMonth.now();
            
            return !expiryDate.isBefore(currentDate);
        } catch (Exception e) {
            return false;
        }
    }
}

