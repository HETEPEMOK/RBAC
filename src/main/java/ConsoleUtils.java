import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine();

            if (!required || !input.trim().isEmpty()) {
                return input.trim();
            }
            System.out.println("This field is required. Please enter a value.");
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            try {
                System.out.print(message);
                int value = Integer.parseInt(scanner.nextLine().trim());

                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Please enter a number between %d and %d.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid integer.");
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " [Y/N]: ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y")) {
                return true;
            }
            if (input.equals("n")) {
                return false;
            }
            System.out.println("Please answer 'y' or 'n'.");
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options.isEmpty()) {
            throw new IllegalArgumentException("No options available");
        }

        System.out.println(message);
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("  %d. %s\n", i + 1, options.get(i).toString());
        }

        int choice = promptInt(scanner, "Enter your choice (1-" + options.size() + "): ", 1, options.size());
        return options.get(choice - 1);
    }

    public static String promptDate(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (YYYY-MM-DD HH:MM): ");
            String date = scanner.nextLine().trim();

            if (ValidationUtils.isValidDate(date)) {
                return date;
            }
            System.out.println("Invalid date format. Please use YYYY-MM-DD HH:MM");
        }
    }

    public static void printSuccess(String message) {
        System.out.println("+ " + message);
    }

    public static void printError(String message) {
        System.out.println("- " + message);
    }

    public static void printWarning(String message) {
        System.out.println("! " + message);
    }

    public static void pressAnyKeyToContinue(Scanner scanner) {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}