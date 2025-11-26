package view;

import java.awt.*;
import java.time.LocalDate;
import java.util.Arrays;
import javax.swing.*;
import model.*;

/**
 * Dialog for adding a new product to inventory.
 */
public class AddProductDialog extends JDialog {
    private final Inventory inventory;
    private boolean productAdded = false;
    
    private JTextField idField;
    private JTextField nameField;
    private JTextField priceField;
    private JTextField quantityField;
    private JTextField subcategoriesField;
    private JTextField brandField;
    private JTextField variantField;
    private JComboBox<String> categoryCombo;
    private JTextField expiryField;
    private JLabel expiryLabel;
    
    /**
     * Constructs an add product dialog.
     *
     * @param parent The parent window
     * @param inventory The inventory to add to
     */
    public AddProductDialog(Window parent, Inventory inventory) {
        super(parent, "Add New Product", ModalityType.APPLICATION_MODAL);
        this.inventory = inventory;
        
        setupUI();
        setSize(450, 550);
        setLocationRelativeTo(parent);
    }
    
    /**
     * Sets up the dialog UI.
     */
    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(25, 118, 210));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Add New Product");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Category (FIRST)
        formPanel.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>(new String[]{
            "Food", "Beverages", "Pharmacy", 
            "Toiletries", "Household & Pet", "General & Specialty"
        });
        categoryCombo.addActionListener(e -> {
            updateExpiryFieldVisibility();
            updateGeneratedID();
        });
        formPanel.add(categoryCombo);
        
        // Product ID (auto-generated, read-only)
        formPanel.add(new JLabel("Product ID:"));
        idField = new JTextField();
        idField.setEditable(false);
        idField.setText(generateProductID(0)); // Initial generation
        formPanel.add(idField);
        
        // Name
        formPanel.add(new JLabel("Product Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);
        
        // Price
        formPanel.add(new JLabel("Price (₱):"));
        priceField = new JTextField();
        formPanel.add(priceField);
        
        // Quantity
        formPanel.add(new JLabel("Initial Stock:"));
        quantityField = new JTextField();
        formPanel.add(quantityField);
        
        // Brand
        formPanel.add(new JLabel("Brand:"));
        brandField = new JTextField();
        formPanel.add(brandField);
        
        // Variant
        formPanel.add(new JLabel("Variant:"));
        variantField = new JTextField();
        formPanel.add(variantField);
        
        // Subcategories
        formPanel.add(new JLabel("Subcategories:"));
        subcategoriesField = new JTextField();
        formPanel.add(subcategoriesField);
        
        JLabel subHelp = new JLabel("(comma-separated)");
        subHelp.setFont(new Font("Arial", Font.ITALIC, 10));
        formPanel.add(new JLabel());
        formPanel.add(subHelp);
        
        // Expiration Date (for perishable items)
        expiryLabel = new JLabel("Expiration Date:");
        formPanel.add(expiryLabel);
        expiryField = new JTextField("YYYY-MM-DD");
        formPanel.add(expiryField);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        JButton addButton = new JButton("Add Product");
        addButton.setBackground(new Color(34, 139, 34));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addProduct());
        buttonPanel.add(addButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        updateExpiryFieldVisibility();
    }
    
    /**
     * Shows/hides expiry field based on category.
     */
    private void updateExpiryFieldVisibility() {
        String category = (String) categoryCombo.getSelectedItem();
        boolean isPerishable = category.equals("Food") || 
                              category.equals("Beverages") || 
                              category.equals("Pharmacy");
        
        expiryLabel.setVisible(isPerishable);
        expiryField.setVisible(isPerishable);
    }
    
    /**
     * Adds the product to inventory.
     */
    private void addProduct() {
        try {
            // Validate and get input
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            double price = Double.parseDouble(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            String brand = brandField.getText().trim();
            String variant = variantField.getText().trim();
            String subcatStr = subcategoriesField.getText().trim();
            
            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "ID and Name are required!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parse subcategories
            java.util.List<String> subcats = Arrays.asList(subcatStr.split(","));
            for (int i = 0; i < subcats.size(); i++) {
                subcats.set(i, subcats.get(i).trim());
            }
            
            Product product = null;
            
            // Create product based on category
            boolean isPerishable = category.equals("Food") || 
                                  category.equals("Beverages") || 
                                  category.equals("Pharmacy");
            
            if (isPerishable) {
                LocalDate expiry = LocalDate.parse(expiryField.getText().trim());
                
                switch (category) {
                    case "Food":
                        product = new Food(id, name, price, quantity, subcats, brand, variant, expiry);
                        break;
                    case "Beverages":
                        product = new Beverages(id, name, price, quantity, subcats, brand, variant, expiry);
                        break;
                    case "Pharmacy":
                        product = new Pharmacy(id, name, price, quantity, subcats, brand, variant, expiry);
                        break;
                }
            } else {
                switch (category) {
                    case "Toiletries":
                        product = new Toiletries(id, name, price, quantity, subcats, brand, variant);
                        break;
                    case "HouseholdAndPet":
                        product = new HouseholdAndPet(id, name, price, quantity, subcats, brand, variant);
                        break;
                    case "GeneralAndSpecialty":
                        product = new GeneralAndSpecialty(id, name, price, quantity, subcats, brand, variant);
                        break;
                }
            }
            
            if (product != null) {
                inventory.addProduct(product);
                productAdded = true;
                
                JOptionPane.showMessageDialog(this,
                    "Product added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Price and Quantity must be valid numbers!",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Returns whether a product was added.
     */
    public boolean isProductAdded() {
        return productAdded;
    }

    /**
     * Updates the generated Product ID based on category selection.
     */
    private void updateGeneratedID() {
        int categoryIndex = categoryCombo.getSelectedIndex();
        String generatedID = generateProductID(categoryIndex);
        idField.setText(generatedID);
    }

    /**
     * Generates a Product ID based on category index.
     *
     * @param categoryIndex (int) The selected category index (0-5).
     * @return (String) The generated Product ID.
     */
    private String generateProductID(int categoryIndex) {
        String prefix;
        switch (categoryIndex) {
            case 0:
                prefix = "F"; // Food
                break;
            case 1:
                prefix = "B"; // Beverages
                break;
            case 2:
                prefix = "P"; // Pharmacy
                break;
            case 3:
                prefix = "T"; // Toiletries
                break;
            case 4:
                prefix = "H"; // Household & Pet
                break;
            case 5:
                prefix = "G"; // General & Specialty
                break;
            default:
                prefix = "X";
        }
        
        // Find the highest number for this prefix
        java.util.List<Product> allProducts = inventory.getAllProducts();
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
}