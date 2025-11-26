package view;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.Customer;
import util.InactivityManager;

/**
 * Dialog for viewing and managing inactive customer accounts.
 * Shows customers inactive for 2+ years and allows bulk deletion.
 */
public class InactiveCustomersDialog extends JDialog {
    private final InactivityManager inactivityManager;
    private JTable inactiveTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;
    
    /**
     * Constructs the InactiveCustomersDialog.
     *
     * @param parent The parent window.
     * @param inactivityManager The inactivity manager instance.
     */
    public InactiveCustomersDialog(Window parent, InactivityManager inactivityManager) {
        super(parent, "Manage Inactive Accounts", ModalityType.APPLICATION_MODAL);
        this.inactivityManager = inactivityManager;
        
        setupUI();
        loadInactiveCustomers();
        setSize(900, 600);
        setLocationRelativeTo(parent);
    }
    
    /**
     * Sets up the dialog UI.
     */
    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(211, 47, 47));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("⚠ Inactive Customer Accounts (2+ Years)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Center: Table panel
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Stats panel
        statsLabel = new JLabel("Loading...");
        statsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statsLabel.setForeground(new Color(211, 47, 47));
        centerPanel.add(statsLabel, BorderLayout.NORTH);
        
        // Table
        centerPanel.add(createTablePanel(), BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom: Action buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Creates the table panel with inactive customers.
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Column names
        String[] columnNames = {
            "User ID", "Name", "Last Activity Date", "Days Inactive", 
            "Membership", "Points"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only
            }
        };
        
        inactiveTable = new JTable(tableModel);
        inactiveTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        inactiveTable.setRowHeight(25);
        inactiveTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        inactiveTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        inactiveTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        inactiveTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        inactiveTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        inactiveTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        inactiveTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        
        // Add sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        inactiveTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(inactiveTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the action buttons panel.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Delete Selected button
        JButton deleteSelectedBtn = new JButton("Delete Selected");
        deleteSelectedBtn.setBackground(new Color(244, 67, 54));
        deleteSelectedBtn.setForeground(Color.WHITE);
        deleteSelectedBtn.setFont(new Font("Arial", Font.BOLD, 12));
        deleteSelectedBtn.addActionListener(e -> handleDeleteSelected());
        panel.add(deleteSelectedBtn);
        
        // Delete All button
        JButton deleteAllBtn = new JButton("Delete All Inactive Accounts");
        deleteAllBtn.setBackground(new Color(211, 47, 47));
        deleteAllBtn.setForeground(Color.WHITE);
        deleteAllBtn.setFont(new Font("Arial", Font.BOLD, 12));
        deleteAllBtn.addActionListener(e -> handleDeleteAll());
        panel.add(deleteAllBtn);
        
        // Refresh button
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            loadInactiveCustomers();
            tableModel.fireTableDataChanged();
        });
        panel.add(refreshBtn);
        
        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        panel.add(closeBtn);
        
        return panel;
    }
    
    /**
     * Loads inactive customers into the table.
     */
    private void loadInactiveCustomers() {
        tableModel.setRowCount(0);
        List<Customer> inactiveCustomers = inactivityManager.getInactiveCustomers();
        
        if (inactiveCustomers.isEmpty()) {
            statsLabel.setText("✓ No inactive accounts found.");
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Customer customer : inactiveCustomers) {
            String membershipStatus = customer.hasMembership() ? "Active" : "None";
            
            tableModel.addRow(new Object[]{
                customer.getUserID(),
                customer.getName(),
                customer.getLastActivityDate().format(formatter),
                customer.getDaysSinceLastActivity(),
                membershipStatus,
                customer.getPoints()
            });
        }
        
        statsLabel.setText("Found " + inactiveCustomers.size() + " inactive account(s)");
    }
    
    /**
     * Handles deletion of selected accounts.
     */
    private void handleDeleteSelected() {
        int[] selectedRows = inactiveTable.getSelectedRows();
        
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one account to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show confirmation with list of selected accounts
        StringBuilder selectedNames = new StringBuilder();
        for (int i = 0; i < selectedRows.length && i < 5; i++) {
            int modelRow = inactiveTable.convertRowIndexToModel(selectedRows[i]);
            selectedNames.append("• ").append(tableModel.getValueAt(modelRow, 0)).append("\n");
        }
        if (selectedRows.length > 5) {
            selectedNames.append("... and ").append(selectedRows.length - 5).append(" more\n");
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete " + selectedRows.length + " account(s)?\n\n" +
            selectedNames.toString() +
            "\nWARNING: This action cannot be undone!",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int deleted = 0;
            
            // Delete from bottom to top to avoid index shifting
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                int modelRow = inactiveTable.convertRowIndexToModel(selectedRows[i]);
                String userID = (String) tableModel.getValueAt(modelRow, 0);
                
                if (inactivityManager.removeInactiveCustomer(userID)) {
                    deleted++;
                }
            }
            
            JOptionPane.showMessageDialog(this,
                "Successfully deleted " + deleted + " account(s).",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            loadInactiveCustomers();
        }
    }
    
    /**
     * Handles deletion of all inactive accounts.
     */
    private void handleDeleteAll() {
        List<Customer> inactiveCustomers = inactivityManager.getInactiveCustomers();
        
        if (inactiveCustomers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No inactive accounts to delete.",
                "Empty",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Strong warning for bulk deletion
        int confirm = JOptionPane.showConfirmDialog(this,
            "⚠ WARNING ⚠\n\n" +
            "You are about to PERMANENTLY DELETE all " + 
            inactiveCustomers.size() + " inactive account(s).\n\n" +
            "This action CANNOT be undone.\n" +
            "All customer data will be lost forever.\n\n" +
            "Are you absolutely sure?",
            "Confirm Bulk Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Second confirmation
        int doubleConfirm = JOptionPane.showConfirmDialog(this,
            "Please confirm again by clicking YES.\n\n" +
            "Deleting " + inactiveCustomers.size() + " account(s)...",
            "Final Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (doubleConfirm == JOptionPane.YES_OPTION) {
            int removed = inactivityManager.removeAllInactiveCustomers();
            
            JOptionPane.showMessageDialog(this,
                "Successfully deleted " + removed + " inactive account(s).",
                "Bulk Deletion Complete",
                JOptionPane.INFORMATION_MESSAGE);
            
            loadInactiveCustomers();
        }
    }
}