package model;
/**
 * The main driver class for the Convenience Store Simulation.
 * Its sole responsibility is to initialize and start the application.
 */
public class StoreSimulation {

    /**
     * The main entry point for the Java application.
     * This method creates the main controller and begins the
     * application's execution loop.
     *
     * @param args (String[]) Command-line arguments (not used).
     */
    public static void main(String[] args) {
        StoreController controller = new StoreController();
        controller.start();
    }
}

