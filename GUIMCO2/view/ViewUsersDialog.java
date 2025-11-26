package view;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.Customer;
import util.StoreDataHandler;

/**
 * Dialog for viewing and managing user accounts.
 * Features: filters, search, sorting, edit capabilities, account removal.
 */
public class ViewUsersDialog extends JDialog {
    private final List<Customer> customerList;
    private final StoreDataHandler dataHandler;
    private List<Customer> filteredCustomers;
    
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Filter controls
    private JComboBox<String> membershipFilterCombo;
    private JTextField searchField;
    private JSpinner minPointsSpinner;
    private JSpinner maxPointsSpinner;
    
    /**
     * Constructs the View Users dialog.
     *
     * @param parent The parent window
     * @param customerList The list of all customers
     * @param dataHandler The data handler for saving changes
     */
    public ViewUsersDialog(Window parent, List<Customer> customerList, StoreDataHandler dataHandler) {
        super(parent, "View Users", ModalityType.APPLICATION_MODAL);
        this.customerList = customerList;
        this.dataHandler = dataHandler;
        this.filteredCustomers = new ArrayList<>(customerList);
        
        setupUI();
        loadTableData();
        setSize(1100, 700);
        setLocationRelativeTo(parent);
    }
    
    /**
     * Sets up the user interface.
     */
    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel with title
        add(createTitlePanel(), BorderLayout.NORTH);
        
        // Center panel with filters and table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(createFilterPanel(), BorderLayout.NORTH);
        centerPanel.add(createTablePanel(), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with action buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Creates the title panel.
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(25, 118, 210));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel);
        
        return panel;
    }
    
    /**
     * Creates the filter panel.
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Filters"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Search field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Search (Name/ID):"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });
        panel.add(searchField, gbc);
        
        // Membership filter
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Membership:"), gbc);
        
        gbc.gridx = 3;
        membershipFilterCombo = new JComboBox<>(new String[]{"All", "Active", "Expired", "None"});
        membershipFilterCombo.addActionListener(e -> applyFilters());
        panel.add(membershipFilterCombo, gbc);
        
        // Points range
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Points Range:"), gbc);
        
        gbc.gridx = 1;
        JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        minPointsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 10));
        maxPointsSpinner = new JSpinner(new SpinnerNumberModel(10000, 0, 10000, 10));
        minPointsSpinner.addChangeListener(e -> applyFilters());
        maxPointsSpinner.addChangeListener(e -> applyFilters());
        
        pointsPanel.add(new JLabel("Min:"));
        pointsPanel.add(minPointsSpinner);
        pointsPanel.add(new JLabel("Max:"));
        pointsPanel.add(maxPointsSpinner);
        panel.add(pointsPanel, gbc);
        
        // Reset filters button
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        JButton resetBtn = new JButton("Reset Filters");
        resetBtn.addActionListener(e -> resetFilters());
        panel.add(resetBtn, gbc);
        
        return panel;
    }
    
    /**
     * Creates the table panel.
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table
        String[] columnNames = {"User ID", "Last Name", "First Name", "Middle Name", 
                                "Membership Card", "Expiry Date", "Points", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only, use edit dialog instead
            }
        };
        
        customerTable = new JTable(tableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.setRowHeight(25);
        customerTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        customerTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        customerTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        customerTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        customerTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        customerTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        customerTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        customerTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        
        // Add sorting
        sorter = new TableRowSorter<>(tableModel);
        customerTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add count label
        JLabel countLabel = new JLabel("Total Users: 0");
        countLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(countLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the button panel.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton editBtn = new JButton("Edit Selected User");
        editBtn.setBackground(new Color(33, 150, 243));
        editBtn.setForeground(Color.WHITE);
        editBtn.addActionListener(e -> handleEditUser());
        panel.add(editBtn);
        
        JButton removeBtn = new JButton("Ban/Remove User");
        removeBtn.setBackground(new Color(244, 67, 54));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.addActionListener(e -> handleRemoveUser());
        panel.add(removeBtn);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            loadTableData();
            applyFilters();
        });
        panel.add(refreshBtn);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        panel.add(closeBtn);
        
        return panel;
    }
    
    /**
     * Loads customer data into the table.
     */
    private void loadTableData() {
        tableModel.setRowCount(0);
        filteredCustomers = new ArrayList<>(customerList);
        
        for (Customer customer : filteredCustomers) {
            if (customer.isGuest()) {
                continue; // Skip guest users
            }
            
            String membershipCard = customer.getMembershipCardID() != null ? 
                customer.getMembershipCardID() : "None";
            
            String expiryDate = customer.getCardExpiryDate() != null ? 
                customer.getCardExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";
            
            String status;
            if (customer.getMembershipCardID() == null) {
                status = "No Membership";
            } else if (customer.hasMembership()) {
                status = "Active";
            } else {
                status = "Expired";
            }
            
            tableModel.addRow(new Object[]{
                customer.getUserID(),
                customer.getLastName(),
                customer.getFirstName(),
                customer.getMiddleName(),
                membershipCard,
                expiryDate,
                customer.getPoints(),
                status
            });
        }
        
        updateCountLabel();
    }
    
    /**
     * Applies the selected filters.
     */
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String membershipFilter = (String) membershipFilterCombo.getSelectedItem();
        int minPoints = (Integer) minPointsSpinner.getValue();
        int maxPoints = (Integer) maxPointsSpinner.getValue();
        
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        
        // Search filter
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText));
        }
        
        // Membership filter
        if (!"All".equals(membershipFilter)) {
            filters.add(RowFilter.regexFilter(membershipFilter, 7)); // Status column
        }
        
        // Points range filter
        filters.add(new RowFilter<Object, Object>() {
            @Override
            public boolean include(Entry<? extends Object, ? extends Object> entry) {
                int points = (Integer) entry.getValue(6);
                return points >= minPoints && points <= maxPoints;
            }
        });
        
        if (!filters.isEmpty()) {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        } else {
            sorter.setRowFilter(null);
        }
        
        updateCountLabel();
    }
    
    /**
     * Resets all filters.
     */
    private void resetFilters() {
        searchField.setText("");
        membershipFilterCombo.setSelectedIndex(0);
        minPointsSpinner.setValue(0);
        maxPointsSpinner.setValue(10000);
        sorter.setRowFilter(null);
        updateCountLabel();
    }
    
    /**
     * Updates the count label.
     */
    private void updateCountLabel() {
        JPanel tablePanel = (JPanel) ((JPanel) getContentPane().getComponent(1)).getComponent(1);
        JLabel countLabel = (JLabel) tablePanel.getComponent(1);
        countLabel.setText("Total Users: " + customerTable.getRowCount());
    }
    
    /**
     * Handles editing a user.
     */
    private void handleEditUser() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a user to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = customerTable.convertRowIndexToModel(selectedRow);
        String userID = (String) tableModel.getValueAt(modelRow, 0);
        
        Customer customer = findCustomerByID(userID);
        if (customer == null) {
            JOptionPane.showMessageDialog(this,
                "Customer not found!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Open edit dialog
        EditUserDialog editDialog = new EditUserDialog(this, customer, dataHandler, customerList);
        editDialog.setVisible(true);
        
        if (editDialog.isChangesSaved()) {
            loadTableData();
            applyFilters();
        }
    }
    
    /**
     * Handles removing/banning a user.
     */
    private void handleRemoveUser() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a user to remove.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = customerTable.convertRowIndexToModel(selectedRow);
        String userID = (String) tableModel.getValueAt(modelRow, 0);
        String userName = tableModel.getValueAt(modelRow, 1) + ", " + tableModel.getValueAt(modelRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to ban/remove this user?\n\n" +
            "User ID: " + userID + "\n" +
            "Name: " + userName + "\n\n" +
            "WARNING: This action cannot be undone!\n" +
            "The User ID will NEVER be reused.",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Customer customer = findCustomerByID(userID);
            if (customer != null) {
                customerList.remove(customer);
                dataHandler.saveCustomers(customerList);
                
                JOptionPane.showMessageDialog(this,
                    "User removed successfully!\nUser ID " + userID + " is now banned.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                loadTableData();
                applyFilters();
            }
        }
    }
    
    /**
     * Finds a customer by user ID.
     */
    private Customer findCustomerByID(String userID) {
        for (Customer c : customerList) {
            if (c.getUserID().equals(userID)) {
                return c;
            }
        }
        return null;
    }
}
