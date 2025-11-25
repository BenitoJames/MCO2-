package view;

import controller.StoreControllerGUI;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Employee management interface.
 * Allows inventory management, customer management, and viewing sales.
 */
public class EmployeeGUI extends JPanel {
    private final StoreControllerGUI controller;
    private final Inventory inventory;
    private final List<Customer> customerList;
    
    private JTextArea displayArea;
    
    /**
     * Constructs the employee management interface.
     *
     * @param controller The main GUI controller
     * @param inventory The store inventory
     * @param customerList The list of customers
     */
    public EmployeeGUI(StoreControllerGUI controller, Inventory inventory, List<Customer> customerList) {
        this.controller = controller;
        this.inventory = inventory;
        this.customerList = customerList;
        
        setupUI();
    }
    
    /**
     * Sets up the user interface.
     */
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Top panel
        add(createTopPanel(), BorderLayout.NORTH);
        
        // Center: Split pane with menu and display
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createMenuPanel());
        splitPane.setRightComponent(createDisplayPanel());
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);
        
        // Bottom panel
        add(createBottomPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Creates the top panel with title.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(25, 118, 210));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Employee Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel);
        
        return panel;
    }
    
    /**
     * Creates the menu panel with action buttons.
     */
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        panel.setBackground(new Color(240, 240, 240));
        
        // Menu title
        JLabel menuTitle = new JLabel("Actions");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 18));
        menuTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(menuTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Inventory Management
        panel.add(createMenuButton("View Inventory", e -> displayInventory()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(createMenuButton("Add Product", e -> handleAddProduct()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(createMenuButton("Restock Item", e -> handleRestock()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(createMenuButton("Low Stock Items", e -> displayLowStock()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(createMenuButton("Expiring Items", e -> displayExpiringItems()));
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Customer Management
        panel.add(createMenuButton("View Customers", e -> displayCustomers()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(createMenuButton("Add Customer", e -> handleAddCustomer()));
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Sales
        panel.add(createMenuButton("View Sales Log", e -> controller.showSalesLog()));
        
        return panel;
    }
    
    /**
     * Creates the display panel for showing information.
     */
    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Information Display"));
        
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        displayArea.setText("Welcome! Select an action from the menu.");
        
        JScrollPane scrollPane = new JScrollPane(displayArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the bottom panel with navigation buttons.
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(new Color(245, 245, 245));
        
        JButton saveButton = new JButton("Save All Changes");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setBackground(new Color(34, 139, 34));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> controller.saveAllData());
        panel.add(saveButton);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.addActionListener(e -> controller.returnToMainMenu());
        panel.add(logoutButton);
        
        return panel;
    }
    
    /**
     * Creates a styled menu button.
     */
    private JButton createMenuButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(220, 40));
        button.addActionListener(listener);
        return button;
    }
    
    // --- Display Methods ---
    
    private void displayInventory() {
        displayArea.setText(inventory.viewAllInventory());
    }
    
    private void displayLowStock() {
        List<Product> lowStock = inventory.getLowStockItems();
        StringBuilder sb = new StringBuilder();
        sb.append("=== LOW STOCK ITEMS ===\n\n");
        
        if (lowStock.isEmpty()) {
            sb.append("No low stock items.");
        } else {
            for (Product p : lowStock) {
                sb.append(p.displayDetails()).append("\n");
            }
        }
        
        displayArea.setText(sb.toString());
    }
    
    private void displayExpiringItems() {
        List<PerishableProduct> expiring = inventory.getExpiringItems();
        StringBuilder sb = new StringBuilder();
        sb.append("=== EXPIRING ITEMS ===\n\n");
        
        if (expiring.isEmpty()) {
            sb.append("No expiring items.");
        } else {
            for (PerishableProduct p : expiring) {
                sb.append(p.displayDetails()).append("\n");
            }
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Remove all expiring items from inventory?",
                "Remove Expiring Items",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                List<PerishableProduct> removed = inventory.removeExpiringItems();
                sb.append("\n\nRemoved ").append(removed.size()).append(" item(s).");
                controller.saveInventoryChanges();
            }
        }
        
        displayArea.setText(sb.toString());
    }
    
    private void displayCustomers() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CUSTOMER LIST ===\n\n");
        
        if (customerList.isEmpty()) {
            sb.append("No customers registered.");
        } else {
            for (Customer c : customerList) {
                sb.append(c.toString()).append("\n");
            }
        }
        
        displayArea.setText(sb.toString());
    }
    
    // --- Action Handlers ---
    
    private void handleAddProduct() {
        // Create dialog for adding product
        AddProductDialog dialog = new AddProductDialog(
            SwingUtilities.getWindowAncestor(this),
            inventory
        );
        dialog.setVisible(true);
        
        if (dialog.isProductAdded()) {
            controller.saveInventoryChanges();
            displayInventory();
        }
    }
    
    private void handleRestock() {
        String productID = JOptionPane.showInputDialog(this,
            "Enter Product ID to restock:",
            "Restock Product",
            JOptionPane.QUESTION_MESSAGE);
        
        if (productID != null && !productID.trim().isEmpty()) {
            Product product = inventory.findProductByID(productID.trim());
            
            if (product == null) {
                JOptionPane.showMessageDialog(this,
                    "Product not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String amountStr = JOptionPane.showInputDialog(this,
                "Current stock: " + product.getQuantityInStock() + "\nEnter amount to add:",
                "Restock",
                JOptionPane.QUESTION_MESSAGE);
            
            if (amountStr != null) {
                try {
                    int amount = Integer.parseInt(amountStr);
                    if (amount > 0) {
                        product.setQuantityInStock(product.getQuantityInStock() + amount);
                        JOptionPane.showMessageDialog(this,
                            "Stock updated!\nNew stock: " + product.getQuantityInStock(),
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        controller.saveInventoryChanges();
                        displayInventory();
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                        "Invalid number!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void handleAddCustomer() {
        // Create dialog for adding customer
        AddCustomerDialog dialog = new AddCustomerDialog(
            SwingUtilities.getWindowAncestor(this),
            customerList
        );
        dialog.setVisible(true);
        
        if (dialog.isCustomerAdded()) {
            controller.saveCustomerChanges();
            displayCustomers();
        }
    }
}