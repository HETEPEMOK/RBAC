import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

@DisplayName("ConsoleUtils Tests")
class ConsoleUtilsTest {

    private Scanner createScanner(String input) {
        InputStream in = new ByteArrayInputStream(input.getBytes());
        return new Scanner(in);
    }

    @Nested
    @DisplayName("Prompt String Tests")
    class PromptStringTests {
        @Test
        @DisplayName("Should prompt for required string")
        void promptRequiredString() {
            Scanner scanner = createScanner("test\n");
            String result = ConsoleUtils.promptString(scanner, "Enter: ", true);
            assertEquals("test", result);
        }

        @Test
        @DisplayName("Should retry on empty required string")
        void retryOnEmptyRequired() {
            Scanner scanner = createScanner("\n\nvalid\n");
            String result = ConsoleUtils.promptString(scanner, "Enter: ", true);
            assertEquals("valid", result);
        }

        @Test
        @DisplayName("Should allow empty optional string")
        void allowEmptyOptional() {
            Scanner scanner = createScanner("\n");
            String result = ConsoleUtils.promptString(scanner, "Enter: ", false);
            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Prompt Int Tests")
    class PromptIntTests {
        @Test
        @DisplayName("Should prompt for valid integer")
        void promptValidInt() {
            Scanner scanner = createScanner("5\n");
            int result = ConsoleUtils.promptInt(scanner, "Enter: ", 1, 10);
            assertEquals(5, result);
        }

        @Test
        @DisplayName("Should retry on invalid integer")
        void retryOnInvalidInt() {
            Scanner scanner = createScanner("abc\n15\n7\n");
            int result = ConsoleUtils.promptInt(scanner, "Enter: ", 1, 10);
            assertEquals(7, result);
        }

        @Test
        @DisplayName("Should reject out of range values")
        void rejectOutOfRange() {
            Scanner scanner = createScanner("0\n11\n5\n");
            int result = ConsoleUtils.promptInt(scanner, "Enter: ", 1, 10);
            assertEquals(5, result);
        }
    }

    @Nested
    @DisplayName("Prompt Yes/No Tests")
    class PromptYesNoTests {
        @Test
        @DisplayName("Should accept yes responses")
        void acceptYes() {
            assertTrue(ConsoleUtils.promptYesNo(createScanner("y\n"), "Continue?"));
            assertTrue(ConsoleUtils.promptYesNo(createScanner("Y\n"), "Continue?"));
        }

        @Test
        @DisplayName("Should accept no responses")
        void acceptNo() {
            assertFalse(ConsoleUtils.promptYesNo(createScanner("n\n"), "Continue?"));
            assertFalse(ConsoleUtils.promptYesNo(createScanner("N\n"), "Continue?"));
        }

        @Test
        @DisplayName("Should retry on invalid response")
        void retryOnInvalid() {
            Scanner scanner = createScanner("maybe\ny\n");
            assertTrue(ConsoleUtils.promptYesNo(scanner, "Continue?"));
        }
    }

    @Nested
    @DisplayName("Prompt Choice Tests")
    class PromptChoiceTests {
        @Test
        @DisplayName("Should select from list")
        void selectFromList() {
            Scanner scanner = createScanner("2\n");
            List<String> options = List.of("Option 1", "Option 2", "Option 3");

            String result = ConsoleUtils.promptChoice(scanner, "Choose:", options);
            assertEquals("Option 2", result);
        }

        @Test
        @DisplayName("Should handle empty options")
        void handleEmptyOptions() {
            Scanner scanner = createScanner("1\n");
            List<String> options = List.of();

            assertThrows(IllegalArgumentException.class,
                    () -> ConsoleUtils.promptChoice(scanner, "Choose:", options));
        }

        @Test
        @DisplayName("Should retry on invalid choice")
        void retryOnInvalidChoice() {
            Scanner scanner = createScanner("0\n4\n2\n");
            List<String> options = List.of("A", "B", "C");

            String result = ConsoleUtils.promptChoice(scanner, "Choose:", options);
            assertEquals("B", result);
        }
    }

    @Nested
    @DisplayName("Prompt Date Tests")
    class PromptDateTests {
        @Test
        @DisplayName("Should accept valid date")
        void acceptValidDate() {
            Scanner scanner = createScanner("2024-12-25 14:30\n");
            String result = ConsoleUtils.promptDate(scanner, "Enter date:");
            assertEquals("2024-12-25 14:30", result);
        }

        @Test
        @DisplayName("Should retry on invalid date")
        void retryOnInvalidDate() {
            Scanner scanner = createScanner("invalid\n25-12-2024\n2024-12-25 14:30\n");
            String result = ConsoleUtils.promptDate(scanner, "Enter date:");
            assertEquals("2024-12-25 14:30", result);
        }
    }
}