package view;

import controller.StoreControllerGUI;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import model.*;

/**
 * Customer shopping interface.
 * Displays products, manages shopping cart, and handles checkout.
 */
public class CustomerGUI extends JPanel {
    private final StoreControllerGUI controller;
    private final Inventory inventory;
    private final Customer customer;
    private final List<CartItem> shoppingCart;
    
    // UI Components
    private JPanel productsPanel;
    private JTextArea cartArea;
    private JLabel totalLabel;
    private JLabel pointsLabel;
    // New cart table UI
    private JTable cartTable;
    private CartTableModel cartTableModel;
    private JLabel cartItemCountLabel;
    private JLabel cartTotalLabel;
    private final NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"));
    
    private double currentTotal;
    
    /**
     * Constructs the customer shopping interface.
     *
     * @param controller The main GUI controller
     * @param inventory The store inventory
     * @param customer The current customer
     */
    public CustomerGUI(StoreControllerGUI controller, Inventory inventory, Customer customer) {
        this.controller = controller;
        this.inventory = inventory;
        this.customer = customer;
        this.shoppingCart = new ArrayList<>();
        this.currentTotal = 0.0;
        
        setupUI();
        loadProducts();
        updateCartDisplay();
    }
    
    /**
     * Sets up the user interface.
     */
    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Top panel with customer info
        add(createTopPanel(), BorderLayout.NORTH);
        
        // Center: Split pane with products and cart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createProductsPanel());
        splitPane.setRightComponent(createCartPanel());
        splitPane.setDividerLocation(550);
        splitPane.setResizeWeight(0.6);
        add(splitPane, BorderLayout.CENTER);
        
        // Bottom panel with action buttons
        add(createBottomPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Creates the top panel with customer information.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 100, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Shopping - " + customer.getName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);
        
        // Points display (if member)
        if (customer.hasMembership()) {
            pointsLabel = new JLabel("Points: " + customer.getMembershipCard().getPoints());
            pointsLabel.setFont(new Font("Arial", Font.BOLD, 18));
            pointsLabel.setForeground(Color.YELLOW);
            panel.add(pointsLabel, BorderLayout.EAST);
        }
        
        return panel;
    }
    
    /**
     * Creates the products display panel.
     */
    private JPanel createProductsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Available Products"));
        
        productsPanel = new JPanel();
        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        productsPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    /**
     * Creates the shopping cart panel.
     */
    private JPanel createCartPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));

        // Table-based cart view
        cartTableModel = new CartTableModel(shoppingCart);
        cartTable = new JTable(cartTableModel);
        cartTable.setFillsViewportHeight(true);
        cartTable.setRowHeight(24);
        cartTable.setAutoCreateRowSorter(true);

        // Quantity column uses a spinner editor (column index 4)
        cartTable.getColumnModel().getColumn(4).setCellEditor(new SpinnerEditor());

        JScrollPane tableScroll = new JScrollPane(cartTable);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        // Summary panel (items + total)
        JPanel summaryPanel = new JPanel(new GridLayout(2, 1));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        summaryPanel.setBackground(new Color(240, 240, 240));

        cartItemCountLabel = new JLabel("Items: 0");
        cartItemCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cartTotalLabel = new JLabel("Total: " + currencyFmt.format(0));
        cartTotalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        summaryPanel.add(cartItemCountLabel);
        summaryPanel.add(cartTotalLabel);

        mainPanel.add(summaryPanel, BorderLayout.SOUTH);

        updateCartSummary();
        return mainPanel;
    }
    
    /**
     * Creates the bottom action buttons panel.
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(new Color(245, 245, 245));
        
        JButton clearCartButton = new JButton("Clear Cart");
        clearCartButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearCartButton.addActionListener(e -> clearCart());
        panel.add(clearCartButton);
        
        JButton checkoutButton = new JButton("Proceed to Checkout");
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        checkoutButton.setBackground(new Color(34, 139, 34));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.addActionListener(e -> handleCheckout());
        panel.add(checkoutButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(new Color(211, 47, 47));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> handleCancel());
        panel.add(cancelButton);
        
        return panel;
    }
    
    /**
     * Loads all products from inventory and displays them.
     */
    private void loadProducts() {
        productsPanel.removeAll();
        List<Product> allProducts = inventory.getAllProducts();
        
        for (Product product : allProducts) {
            if (product.getQuantityInStock() > 0) {
                productsPanel.add(createProductCard(product));
                productsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        productsPanel.revalidate();
        productsPanel.repaint();
    }
    
    /**
     * Creates a product card UI component.
     */
    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // Product info
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(nameLabel);
        
        JLabel priceLabel = new JLabel("₱" + String.format("%.2f", product.getPrice()));
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoPanel.add(priceLabel);
        
        JLabel stockLabel = new JLabel("Stock: " + product.getQuantityInStock());
        stockLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        stockLabel.setForeground(Color.GRAY);
        infoPanel.add(stockLabel);
        
        card.add(infoPanel, BorderLayout.CENTER);
        
        // Add to cart button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JSpinner quantitySpinner = new JSpinner(
            new SpinnerNumberModel(1, 1, product.getQuantityInStock(), 1)
        );
        quantitySpinner.setPreferredSize(new Dimension(60, 25));
        buttonPanel.add(quantitySpinner);
        
        JButton addButton = new JButton("Add");
        addButton.setBackground(new Color(34, 139, 34));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addToCart(product, (Integer) quantitySpinner.getValue()));
        buttonPanel.add(addButton);
        
        card.add(buttonPanel, BorderLayout.EAST);
        
        return card;
    }
    
    /**
     * Adds a product to the shopping cart.
     */
    private void addToCart(Product product, int quantity) {
        if (quantity > product.getQuantityInStock()) {
            JOptionPane.showMessageDialog(this,
                "Insufficient stock!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Reduce stock immediately
        product.setQuantityInStock(product.getQuantityInStock() - quantity);
        
        // Check if product already in cart
        CartItem existingItem = null;
        for (CartItem item : shoppingCart) {
            if (item.getProduct().getProductID().equals(product.getProductID())) {
                existingItem = item;
                break;
            }
        }
        
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            shoppingCart.add(new CartItem(product, quantity));
        }
        
        updateCartDisplay();
        loadProducts(); // Refresh product list to show updated stock
        updateCartSummary();
    }
    
    /**
     * Updates the cart display area.
     */
    private void updateCartDisplay() {
        StringBuilder sb = new StringBuilder();
        currentTotal = 0.0;
        
        if (shoppingCart.isEmpty()) {
            sb.append("Your cart is empty.");
        } else {
            sb.append("Items in your cart:\n");
            sb.append("================================\n\n");
            
            for (CartItem item : shoppingCart) {
                sb.append(String.format("%dx %s\n", 
                    item.getQuantity(), 
                    item.getProduct().getName()));
                sb.append(String.format("   ₱%.2f each = ₱%.2f\n\n",
                    item.getProduct().getPrice(),
                    item.getSubtotal()));
                currentTotal += item.getSubtotal();
            }
        }
        // Old text-area cart is optional now; guard in case it's not present
        if (cartArea != null) {
            cartArea.setText(sb.toString());
        }
        if (totalLabel != null) {
            totalLabel.setText("Total: ₱" + String.format("%.2f", currentTotal));
        }
        updateCartSummary();
    }

    private void updateCartSummary() {
        int totalItems = 0;
        double total = 0.0;
        for (CartItem ci : shoppingCart) {
            totalItems += ci.getQuantity();
            total += ci.getQuantity() * ci.getProduct().getPrice();
        }
        if (cartItemCountLabel != null) {
            cartItemCountLabel.setText("Items: " + totalItems);
        }
        if (cartTotalLabel != null) {
            cartTotalLabel.setText("Total: " + currencyFmt.format(total));
        }
        if (cartTableModel != null) {
            cartTableModel.fireTableDataChanged();
        }
    }

    private class CartTableModel extends AbstractTableModel {
        private final String[] cols = { "ID", "Name", "Category", "Unit Price", "Quantity", "Subtotal" };
        private final List<CartItem> cart;

        CartTableModel(List<CartItem> cart) {
            this.cart = cart;
        }

        @Override
        public int getRowCount() { return cart.size(); }

        @Override
        public int getColumnCount() { return cols.length; }

        @Override
        public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CartItem ci = cart.get(rowIndex);
            if (columnIndex == 0) return ci.getProduct().getProductID();
            if (columnIndex == 1) return ci.getProduct().getName();
            if (columnIndex == 2) {
                String id = ci.getProduct().getProductID();
                if (id == null) return "";
                int dash = id.indexOf('-');
                return (dash > 0) ? id.substring(0, dash) : "";
            }
            if (columnIndex == 3) return currencyFmt.format(ci.getProduct().getPrice());
            if (columnIndex == 4) return ci.getQuantity();
            if (columnIndex == 5) return currencyFmt.format(ci.getProduct().getPrice() * ci.getQuantity());
            return "";
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 4; // Only Quantity editable
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 4) {
                int newQty;
                try { newQty = Integer.parseInt(String.valueOf(aValue)); } catch (NumberFormatException e) { return; }
                if (newQty <= 0) return;
                CartItem ci = cart.get(rowIndex);
                int oldQty = ci.getQuantity();
                if (newQty == oldQty) return;

                // Inventory-safe adjustment
                Product product = ci.getProduct();
                int delta = newQty - oldQty;

                if (delta > 0) {
                    // Need to take additional units from stock
                    if (product.getQuantityInStock() >= delta) {
                        product.setQuantityInStock(product.getQuantityInStock() - delta);
                        ci.setQuantity(newQty);
                    } else {
                        // Not enough stock; ignore change and optionally notify
                        JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(cartTable),
                            "Insufficient stock to increase quantity.",
                            "Stock Limit",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                } else {
                    // delta < 0: return units to stock
                    int refund = -delta;
                    product.setQuantityInStock(product.getQuantityInStock() + refund);
                    ci.setQuantity(newQty);
                }

                // Refresh product list to reflect new stock levels
                loadProducts();
                fireTableRowsUpdated(rowIndex, rowIndex);
                updateCartSummary();
            }
        }
    }

    private class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {
        private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            int val = 1;
            try { val = Integer.parseInt(String.valueOf(value)); } catch (NumberFormatException e) {}
            spinner.setValue(val);
            return spinner;
        }
    }
    
    /**
     * Clears the shopping cart and returns stock.
     */
    private void clearCart() {
        if (shoppingCart.isEmpty()) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Clear all items from cart?",
            "Clear Cart",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Return stock
            for (CartItem item : shoppingCart) {
                Product product = item.getProduct();
                product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
            }
            
            shoppingCart.clear();
            updateCartDisplay();
            loadProducts();
        }
    }
    
    /**
     * Handles the checkout process.
     */
    private void handleCheckout() {
        if (shoppingCart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Your cart is empty!",
                "Cannot Checkout",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create transaction
        Transaction transaction = new Transaction(customer);
        for (CartItem item : shoppingCart) {
            transaction.addItem(item);
        }
        
        // Calculate totals
        transaction.calculateTotals(customer.getIsSenior());
        
        // Show payment dialog
        PaymentDialog dialog = new PaymentDialog(
            SwingUtilities.getWindowAncestor(this),
            transaction,
            customer
        );
        dialog.setVisible(true);
        
        if (dialog.isTransactionCompleted()) {
            // Update customer points
            if (customer.hasMembership()) {
                customer.earnPoints(transaction.getAmountForPointsEarning());
            }
            
            controller.completeTransaction(transaction);
        }
    }
    
    /**
     * Handles cancelling the shopping session.
     */
    private void handleCancel() {
        // Return all stock
        for (CartItem item : shoppingCart) {
            Product product = item.getProduct();
            product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
        }
        
        controller.cancelCustomerShopping();
    }
}