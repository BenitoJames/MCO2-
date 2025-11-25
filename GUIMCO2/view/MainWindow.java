package view;

import java.awt.*;
import javax.swing.*;

public class MainWindow extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainWindow() {
        super("DLSU Convenience Store");  // safer

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // CardLayout for switching panels
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Temporary screen just to confirm GUI works
        JPanel temp = new JPanel();
        temp.add(new JLabel("GUI Loaded Successfully!"));

        mainPanel.add(temp, "TEMP");

        add(mainPanel);
        setVisible(true);
    }

    // Allows adding screens from other classes
    public void addScreen(JPanel panel, String name) {
        mainPanel.add(panel, name);
    }

    // Allows switching screens
    public void showScreen(String name) {
        cardLayout.show(mainPanel, name);
    }
}
