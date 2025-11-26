package util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Customer;

/**
 * Manages inactive customer accounts.
 * Handles detection and removal of accounts inactive for 2+ years.
 */
public class InactivityManager {
    private final List<Customer> customerList;
    private final StoreDataHandler dataHandler;
    private static final long INACTIVITY_THRESHOLD_DAYS = 730; // 2 years
    
    /**
     * Constructs an InactivityManager.
     *
     * @param customerList The list of customers to manage.
     * @param dataHandler The data handler for saving changes.
     */
    public InactivityManager(List<Customer> customerList, StoreDataHandler dataHandler) {
        this.customerList = customerList;
        this.dataHandler = dataHandler;
    }
    
    /**
     * Gets all inactive customers (inactive for 2+ years).
     *
     * @return A list of customers inactive for 2+ years.
     */
    public List<Customer> getInactiveCustomers() {
        List<Customer> inactiveCustomers = new ArrayList<>();
        
        for (Customer c : customerList) {
            if (!c.isGuest() && c.isInactiveForTwoYears()) {
                inactiveCustomers.add(c);
            }
        }
        
        return inactiveCustomers;
    }
    
    /**
     * Gets customers approaching inactivity threshold (1.5+ years).
     * Useful for sending warning notifications.
     *
     * @return A list of customers at risk of account deletion.
     */
    public List<Customer> getAtRiskCustomers() {
        List<Customer> atRiskCustomers = new ArrayList<>();
        long warningThreshold = 547; // 1.5 years
        
        for (Customer c : customerList) {
            if (!c.isGuest()) {
                long daysSinceActivity = c.getDaysSinceLastActivity();
                if (daysSinceActivity > warningThreshold && daysSinceActivity <= INACTIVITY_THRESHOLD_DAYS) {
                    atRiskCustomers.add(c);
                }
            }
        }
        
        return atRiskCustomers;
    }
    
    /**
     * Removes a single inactive customer by user ID.
     *
     * @param userID The ID of the customer to remove.
     * @return True if successfully removed, false otherwise.
     */
    public boolean removeInactiveCustomer(String userID) {
        Customer customer = findCustomerByID(userID);
        
        if (customer == null) {
            return false;
        }
        
        if (!customer.isInactiveForTwoYears()) {
            return false;
        }
        
        customerList.remove(customer);
        dataHandler.saveCustomers(customerList);
        return true;
    }
    
    /**
     * Removes all inactive customers and returns the count.
     *
     * @return The number of customers removed.
     */
    public int removeAllInactiveCustomers() {
        List<Customer> toRemove = new ArrayList<>(getInactiveCustomers());
        
        for (Customer c : toRemove) {
            customerList.remove(c);
        }
        
        if (!toRemove.isEmpty()) {
            dataHandler.saveCustomers(customerList);
        }
        
        return toRemove.size();
    }
    
    /**
     * Updates a customer's activity date to today.
     *
     * @param userID The ID of the customer whose activity to record.
     */
    public void recordActivity(String userID) {
        Customer customer = findCustomerByID(userID);
        if (customer != null) {
            customer.updateActivityDate();
            dataHandler.saveCustomers(customerList);
        }
    }
    
    /**
     * Finds a customer by their user ID.
     *
     * @param userID The user ID to search for.
     * @return The customer if found, null otherwise.
     */
    private Customer findCustomerByID(String userID) {
        for (Customer c : customerList) {
            if (c.getUserID().equalsIgnoreCase(userID)) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * Gets a formatted report of inactive customers.
     *
     * @return A formatted report string.
     */
    public String getInactivityReport() {
        List<Customer> inactiveCustomers = getInactiveCustomers();
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== INACTIVE CUSTOMER REPORT ===\n");
        sb.append("Generated: ").append(LocalDate.now()).append("\n\n");
        
        if (inactiveCustomers.isEmpty()) {
            sb.append("No inactive customers found.\n");
        } else {
            sb.append("Total Inactive Customers: ").append(inactiveCustomers.size()).append("\n\n");
            
            for (Customer c : inactiveCustomers) {
                sb.append("User ID: ").append(c.getUserID()).append("\n");
                sb.append("Name: ").append(c.getName()).append("\n");
                sb.append("Last Activity: ").append(c.getLastActivityDate()).append("\n");
                sb.append("Days Inactive: ").append(c.getDaysSinceLastActivity()).append("\n");
                sb.append("Membership: ").append(c.hasMembership() ? "Active" : "None").append("\n");
                sb.append("---\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Gets the inacty threshold in days.
     *
     * @return The number of days (730 = 2 years).
     */
    public long getInactivityThresholdDays() {
        return INACTIVITY_THRESHOLD_DAYS;
    }
}