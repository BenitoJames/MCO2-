package view;

import controller.StoreControllerGUI;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import model.*;

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
     * Creates the menu panel with action buttons and scrolling support.
     */
    private JPanel createMenuPanel() {
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        innerPanel.setBackground(new Color(240, 240, 240));
        
        // Menu title
        JLabel menuTitle = new JLabel("Actions");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 18));
        menuTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel.add(menuTitle);
        innerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Inventory Management
        innerPanel.add(createMenuButton("View Inventory", e -> displayInventory()));
        innerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        innerPanel.add(createMenuButton("Add Product", e -> handleAddProduct()));
        innerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        innerPanel.add(createMenuButton("Restock Item", e -> handleRestock()));
        innerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        innerPanel.add(createMenuButton("Update Product Price", e -> handleUpdateProductPrice()));
        innerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        innerPanel.add(createMenuButton("Update Product Information", e -> handleUpdateProductInfo()));
        innerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        innerPanel.add(createMenuButton("Low Stock Items", e -> displayLowStock()));
        innerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        innerPanel.add(createMenuButton("Expiring Items", e -> displayExpiringItems()));
        innerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // User Management
        innerPanel.add(createMenuButton("View Users", e -> handleViewUsers()));
        innerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        innerPanel.add(createMenuButton("Add User", e -> handleAddCustomer()));
        innerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Sales
        innerPanel.add(createMenuButton("View Sales Log", e -> controller.showSalesLog()));
        
        // Add glue to push buttons to top
        innerPanel.add(Box.createVerticalGlue());
        
        // Wrap in JScrollPane for scrolling
        JScrollPane scrollPane = new JScrollPane(innerPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        
        // Return wrapper panel that contains scroll pane
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Creates the display panel for showing information.
     */
    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Information Display",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        
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
        // Create table model
        String[] columns = {"ID", "Name", "Brand", "Variant", "Price", "Stock", "Category", "Expiry"};
        List<Product> products = inventory.getAllProducts();
        Object[][] data = new Object[products.size()][8];
        
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            data[i][0] = p.getProductID();
            data[i][1] = p.getName();
            data[i][2] = p.getBrand();
            data[i][3] = p.getVariant();
            data[i][4] = String.format("₱%.2f", p.getPrice());
            data[i][5] = p.getQuantityInStock();
            
            // Get category based on ID prefix
            String category = "";
            if (p.getProductID().startsWith("F-")) category = "Food";
            else if (p.getProductID().startsWith("B-")) category = "Beverages";
            else if (p.getProductID().startsWith("T-")) category = "Toiletries";
            else if (p.getProductID().startsWith("H-")) category = "Household";
            else if (p.getProductID().startsWith("P-")) category = "Pharmacy";
            else if (p.getProductID().startsWith("G-")) category = "General";
            data[i][6] = category;
            
            // Expiry date
            if (p instanceof PerishableProduct) {
                data[i][7] = ((PerishableProduct) p).getExpirationDate().toString();
            } else {
                data[i][7] = "N/A";
            }
        }
        
        showTableDialog("All Inventory", columns, data, products.size() + " items");
    }
    
    private void displayLowStock() {
        List<Product> lowStock = inventory.getLowStockItems();
        
        if (lowStock.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No low stock items found!",
                "Low Stock Items",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String[] columns = {"ID", "Name", "Brand", "Variant", "Price", "Stock", "Status"};
        Object[][] data = new Object[lowStock.size()][7];
        
        for (int i = 0; i < lowStock.size(); i++) {
            Product p = lowStock.get(i);
            data[i][0] = p.getProductID();
            data[i][1] = p.getName();
            data[i][2] = p.getBrand();
            data[i][3] = p.getVariant();
            data[i][4] = String.format("₱%.2f", p.getPrice());
            data[i][5] = p.getQuantityInStock();
            
            // Status based on stock level
            int stock = p.getQuantityInStock();
            if (stock == 0) {
                data[i][6] = "OUT OF STOCK";
            } else if (stock <= 3) {
                data[i][6] = "CRITICAL";
            } else {
                data[i][6] = "Low";
            }
        }
        
        showTableDialog("Low Stock Items (≤ 5 units)", columns, data, 
            lowStock.size() + " items need restocking");
    }
    
    private void displayExpiringItems() {
        List<Product> expiring = inventory.getExpiringItems();
        
        if (expiring.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No expiring items found!",
                "Expiring Items",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String[] columns = {"ID", "Name", "Brand", "Variant", "Price", "Stock", "Expiry Date", "Days Left"};
        Object[][] data = new Object[expiring.size()][8];
        
        java.time.LocalDate today = java.time.LocalDate.now();
        
        for (int i = 0; i < expiring.size(); i++) {
            PerishableProduct p = (PerishableProduct) expiring.get(i);
            data[i][0] = p.getProductID();
            data[i][1] = p.getName();
            data[i][2] = p.getBrand();
            data[i][3] = p.getVariant();
            data[i][4] = String.format("₱%.2f", p.getPrice());
            data[i][5] = p.getQuantityInStock();
            data[i][6] = p.getExpirationDate().toString();
            
            // Calculate days until expiry
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, p.getExpirationDate());
            if (daysLeft < 0) {
                data[i][7] = "EXPIRED";
            } else if (daysLeft == 0) {
                data[i][7] = "TODAY";
            } else {
                data[i][7] = daysLeft + " days";
            }
        }
        
        // Show table
        JTable table = new JTable(data, columns);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(900, 400));
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Expiring Items (within 7 days) - " + expiring.size() + " items");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(220, 53, 69));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add remove button
        JButton removeButton = new JButton("Remove All Expiring Items");
        removeButton.setBackground(new Color(220, 53, 69));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFont(new Font("Arial", Font.BOLD, 14));
        removeButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Remove all " + expiring.size() + " expiring items from inventory?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                List<Product> removed = inventory.removeExpiringItems();
                controller.saveInventoryChanges();
                JOptionPane.showMessageDialog(this,
                    "Removed " + removed.size() + " item(s) from inventory.",
                    "Items Removed",
                    JOptionPane.INFORMATION_MESSAGE);
                SwingUtilities.getWindowAncestor(panel).dispose();
                displayInventory(); // Refresh display
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(removeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Expiring Items", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    /**
     * Shows a generic table dialog.
     */
    private void showTableDialog(String title, String[] columns, Object[][] data, String subtitle) {
        JTable table = new JTable(data, columns);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Color code rows based on stock level for inventory views
        if (title.contains("Low Stock")) {
            table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    if (!isSelected) {
                        String status = table.getValueAt(row, 6).toString();
                        if (status.equals("OUT OF STOCK")) {
                            c.setBackground(new Color(255, 200, 200));
                        } else if (status.equals("CRITICAL")) {
                            c.setBackground(new Color(255, 235, 200));
                        } else {
                            c.setBackground(new Color(255, 255, 200));
                        }
                    }
                    
                    return c;
                }
            });
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(900, 400));
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel(title + " - " + subtitle);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    /**
     * Displays the list of customers.
     */
    private void displayCustomers() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== REGISTERED CUSTOMERS ===").append("\n\n");
        
        for (Customer customer : customerList) {
            sb.append("ID: ").append(customer.getUserID()).append("\n");
            sb.append("Name: ").append(customer.getName()).append("\n");
            sb.append("Membership: ");
            if (customer.getMembershipCard() != null) {
                sb.append("Yes (Points: ").append(customer.getPoints()).append(")");
            } else {
                sb.append("No");
            }
            sb.append("\n");
            sb.append("Points: ").append(customer.getPoints()).append("\n");
            sb.append("-".repeat(50)).append("\n");
        }
        
        displayArea.setText(sb.toString());
    }
    
    private void handleViewUsers() {
        ViewUsersDialog dialog = new ViewUsersDialog(
            SwingUtilities.getWindowAncestor(this),
            customerList,
            controller.getDataHandler()
        );
        dialog.setVisible(true);
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
            
            // Keep asking until valid input
            boolean validInput = false;
            while (!validInput) {
                String amountStr = JOptionPane.showInputDialog(this,
                    "Current stock: " + product.getQuantityInStock() + "\nEnter amount to add:",
                    "Restock",
                    JOptionPane.QUESTION_MESSAGE);
                
                if (amountStr == null) {
                    return;
                }
                
                try {
                    int amount = Integer.parseInt(amountStr);
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(this,
                            "Amount must be greater than 0!",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    } else {
                        product.setQuantityInStock(product.getQuantityInStock() + amount);
                        JOptionPane.showMessageDialog(this,
                            "Stock updated!\nNew stock: " + product.getQuantityInStock(),
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        controller.saveInventoryChanges();
                        displayInventory();
                        validInput = true;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                        "Invalid input! Please enter a whole number greater than 0.",
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

    private void handleUpdateProductPrice() {
        String productID = JOptionPane.showInputDialog(this, "Enter Product ID:");
        if (productID == null || productID.trim().isEmpty()) {
            return;
        }
        
        Product product = inventory.findProductByID(productID.trim());
        if (product == null) {
            JOptionPane.showMessageDialog(this,
                "Product not found!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String currentPrice = String.format("%.2f", product.getPrice());
            String newPriceStr = JOptionPane.showInputDialog(this,
                "Product: " + product.getName() + "\nCurrent Price: ₱" + currentPrice + "\n\nEnter new price:",
                currentPrice);
            
            if (newPriceStr == null) {
                return;
            }
            
            double newPrice = Double.parseDouble(newPriceStr);
            if (newPrice < 0.01) {
                JOptionPane.showMessageDialog(this,
                    "Price must be at least 0.01!",
                    "Invalid Price",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            product.setPrice(newPrice);
            controller.saveInventoryChanges();
            JOptionPane.showMessageDialog(this,
                "Price updated successfully!\nNew Price: ₱" + String.format("%.2f", newPrice),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Invalid price format!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateProductInfo() {
        String productID = JOptionPane.showInputDialog(this, "Enter Product ID:");
        if (productID == null || productID.trim().isEmpty()) {
            return;
        }
        
        Product product = inventory.findProductByID(productID.trim());
        if (product == null) {
            JOptionPane.showMessageDialog(this,
                "Product not found!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create a simple dialog for updating product info
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField nameField = new JTextField(product.getName(), 20);
        JTextField brandField = new JTextField(product.getBrand(), 20);
        JTextField variantField = new JTextField(product.getVariant(), 20);
        
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Brand:"));
        panel.add(brandField);
        panel.add(new JLabel("Variant:"));
        panel.add(variantField);
        
        int result = JOptionPane.showConfirmDialog(this, panel,
            "Update Product Information",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            product.setName(nameField.getText());
            product.setBrand(brandField.getText());
            product.setVariant(variantField.getText());
            
            controller.saveInventoryChanges();
            JOptionPane.showMessageDialog(this,
                "Product information updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            displayInventory();
        }
    }
}