package view;

import controller.StoreControllerGUI;
import java.awt.*;
import javax.swing.*;

/**
 * Main menu screen for the convenience store.
 * Displays options for Customer Shopping and Employee Login.
 */
public class StoreGUI extends JPanel {
    private final StoreControllerGUI controller;
    
    /**
     * Constructs the main store menu screen.
     *
     * @param controller The main GUI controller
     */
    public StoreGUI(StoreControllerGUI controller) {
        this.controller = controller;
        setupUI();
    }
    
    /**
     * Sets up the user interface components.
     */
    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        
        // Title Panel
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // Center Panel with buttons
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the title panel with store name.
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(0, 100, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        
        JLabel titleLabel = new JLabel("DLSU Convenience Store");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel);
        
        return panel;
    }
    
    /**
     * Creates the center panel with main action buttons.
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Customer Shopping Button
        JButton customerButton = createStyledButton(
            "🛒 Start Shopping",
            new Color(34, 139, 34),
            new Color(46, 125, 50)
        );
        customerButton.addActionListener(e -> controller.startCustomerShopping());
        customerButton.setPreferredSize(new Dimension(300, 80));
        panel.add(customerButton, gbc);
        
        // Employee Login Button
        gbc.gridy = 1;
        JButton employeeButton = createStyledButton(
            "👤 Employee Login",
            new Color(25, 118, 210),
            new Color(21, 101, 192)
        );
        employeeButton.addActionListener(e -> controller.startEmployeeMode());
        employeeButton.setPreferredSize(new Dimension(300, 80));
        panel.add(employeeButton, gbc);
        
        // Exit Button
        gbc.gridy = 2;
        JButton exitButton = createStyledButton(
            "❌ Exit",
            new Color(211, 47, 47),
            new Color(198, 40, 40)
        );
        exitButton.addActionListener(e -> controller.handleExit());
        exitButton.setPreferredSize(new Dimension(300, 60));
        panel.add(exitButton, gbc);
        
        return panel;
    }
    
    /**
     * Creates the footer panel with store information.
     */
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(60, 60, 60));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel footerLabel = new JLabel("© 2025 DLSU Convenience Store | Open 24/7");
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerLabel.setForeground(Color.WHITE);
        panel.add(footerLabel);
        
        return panel;
    }
    
    /**
     * Creates a styled button with hover effects.
     */
    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
}