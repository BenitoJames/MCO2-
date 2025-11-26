import controller.StoreControllerGUI;
import javax.swing.SwingUtilities;
import view.MainWindow;

public class StoreGUIRunner {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            new StoreControllerGUI(window);
        });
    }
}