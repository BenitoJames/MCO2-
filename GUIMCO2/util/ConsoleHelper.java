package util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * A static utility class to handle all console input.
 * This isolates the Scanner from the controllers and provides
 * robust input validation.
 */
public class ConsoleHelper {

    /**
     * A single, static scanner to be used by the entire application.
     */
    private static Scanner scanner = new Scanner(System.in);

    /**
     * Gets a non-empty string from the user.
     *
     * @param prompt The message to display to the user
     * @return A non-empty string
     */
    public static String getStringInput(String prompt) {
        String input = "";
        boolean isValid = false;
        while (!isValid) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            } else {
                isValid = true;
            }
        }
        return input;
    }

    /**
     * Gets an integer from the user.
     *
     * @param prompt The message to display to the user
     * @return Any integer
     */
    public static int getIntInput(String prompt) {
        int input = 0;
        boolean isValid = false;
        while (!isValid) {
            try {
                System.out.print(prompt);
                input = scanner.nextInt();
                isValid = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            } finally {
                scanner.nextLine(); // Consume the newline character
            }
        }
        return input;
    }

    /**
     * Gets an integer from the user within a specified range.
     *
     * @param prompt The message to display
     * @param min The minimum acceptable value
     * @param max The maximum acceptable value
     * @return An integer within the range [min, max]
     */
    public static int getIntInput(String prompt, int min, int max) {
        int input = 0;
        boolean isValid = false;
        while (!isValid) {
            input = getIntInput(prompt); // Use the other method to get a valid int
            if (input < min || input > max) {
                System.out.println("Input must be between " + min + " and " + max + ".");
            } else {
                isValid = true;
            }
        }
        return input;
    }

    /**
     * Gets a double from the user.
     *
     * @param prompt The message to display
     * @return Any double
     */
    public static double getDoubleInput(String prompt) {
        double input = 0.0;
        boolean isValid = false;
        while (!isValid) {
            try {
                System.out.print(prompt);
                input = scanner.nextDouble();
                isValid = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number (e.g., 12.99).");
            } finally {
                scanner.nextLine(); // Consume the newline character
            }
        }
        return input;
    }

    /**
     * Gets a positive double from the user (must be greater than min).
     *
     * @param prompt The message to display
     * @param min The minimum acceptable value (exclusive)
     * @return A double greater than min
     */
    public static double getDoubleInput(String prompt, double min) {
        double input = 0.0;
        boolean isValid = false;
        while (!isValid) {
            input = getDoubleInput(prompt); // Use the other method
            if (input <= min) {
                System.out.println("Input must be greater than " + min + ".");
            } else {
                isValid = true;
            }
        }
        return input;
    }

    /**
     * Gets a 'yes' or 'no' (y/n) response from the user.
     *
     * @param prompt The message to display (should end with (y/n): )
     * @return true for 'y', false for 'n'
     */
    public static boolean getYesNoInput(String prompt) {
        boolean response = false;
        boolean isValid = false;
        while (!isValid) {
            String input = getStringInput(prompt).toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                response = true;
                isValid = true;
            } else if (input.equals("n") || input.equals("no")) {
                response = false;
                isValid = true;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
        return response;
    }

    /**
     * Gets a date from the user in YYYY-MM-DD format.
     *
     * @param prompt The message to display
     * @return A valid LocalDate object
     */
    public static LocalDate getDateInput(String prompt) {
        LocalDate date = null;
        boolean isValid = false;
        while (!isValid) {
            String input = getStringInput(prompt);
            try {
                date = LocalDate.parse(input); // Default format is YYYY-MM-DD
                isValid = true;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
        return date;
    }

    /**
     * Closes the single static scanner.
     * This should be called once when the application exits.
     */
    public static void closeScanner() {
        scanner.close();
    }
}