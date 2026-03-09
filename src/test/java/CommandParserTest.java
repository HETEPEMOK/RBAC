import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

@DisplayName("CommandParser Tests")
class CommandParserTest {
    private CommandParser parser;
    private RBACSystem system;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();

        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Nested
    @DisplayName("Command Registration Tests")
    class RegistrationTests {
        @Test
        @DisplayName("Should register a command successfully")
        void registerCommand() {
            parser.registerCommand("test", "Test command", (scanner, sys) -> {
                System.out.println("Test executed");
            });

            // Verify by executing
            parser.executeCommand("test", new Scanner(System.in), system);
            assertTrue(outContent.toString().contains("Test executed"));
        }

        @Test
        @DisplayName("Should overwrite existing command with same name")
        void overwriteCommand() {
            parser.registerCommand("test", "First", (scanner, sys) -> {
                System.out.println("First");
            });
            parser.registerCommand("test", "Second", (scanner, sys) -> {
                System.out.println("Second");
            });

            parser.executeCommand("test", new Scanner(System.in), system);
            assertTrue(outContent.toString().contains("Second"));
            assertFalse(outContent.toString().contains("First"));
        }
    }

    @Nested
    @DisplayName("Command Execution Tests")
    class ExecutionTests {
        @Test
        @DisplayName("Should execute existing command")
        void executeExistingCommand() {
            parser.registerCommand("hello", "Say hello", (scanner, sys) -> {
                System.out.println("Hello, World!");
            });

            parser.executeCommand("hello", new Scanner(System.in), system);
            assertTrue(outContent.toString().contains("Hello, World!"));
        }

        @Test
        @DisplayName("Should handle unknown command gracefully")
        void executeUnknownCommand() {
            parser.executeCommand("nonexistent", new Scanner(System.in), system);
            assertTrue(outContent.toString().contains("Unknown command"));
        }

        @Test
        @DisplayName("Should handle case-insensitive command names")
        void caseInsensitiveCommand() {
            parser.registerCommand("TEST", "Test command", (scanner, sys) -> {
                System.out.println("Executed");
            });

            parser.executeCommand("test", new Scanner(System.in), system);
            assertTrue(outContent.toString().contains("Executed"));
        }

        @Test
        @DisplayName("Should handle command execution errors gracefully")
        void handleCommandError() {
            parser.registerCommand("error", "Error command", (scanner, sys) -> {
                throw new RuntimeException("Test error");
            });

            outContent.reset();
            errContent.reset();
            parser.executeCommand("error", new Scanner(System.in), system);
            String errorOutput = errContent.toString();
            assertTrue(errorOutput.contains("Error executing command: Test error message") ||
                    errorOutput.contains("Error executing command"));
        }
    }

    @Nested
    @DisplayName("Parse and Execute Tests")
    class ParseAndExecuteTests {
        @Test
        @DisplayName("Should parse and execute command from input string")
        void parseAndExecute() {
            parser.registerCommand("ping", "Ping command", (scanner, sys) -> {
                System.out.println("pong");
            });

            parser.parseAndExecute("ping", new Scanner(System.in), system);
            assertTrue(outContent.toString().contains("pong"));
        }

        @Test
        @DisplayName("Should ignore empty input")
        void ignoreEmptyInput() {
            parser.parseAndExecute("", new Scanner(System.in), system);
            parser.parseAndExecute("   ", new Scanner(System.in), system);
            assertEquals("", outContent.toString().trim());
        }
    }

    @Nested
    @DisplayName("Help Command Tests")
    class HelpTests {
        @Test
        @DisplayName("Should print help with registered commands")
        void printHelp() {
            parser.registerCommand("cmd1", "Description 1", (scanner, sys) -> {});
            parser.registerCommand("cmd2", "Description 2", (scanner, sys) -> {});
            parser.registerCommand("cmd3", "Description 3", (scanner, sys) -> {});

            parser.printHelp();

            String output = outContent.toString();
            assertTrue(output.contains("AVAILABLE COMMANDS"));
            assertTrue(output.contains("cmd1"));
            assertTrue(output.contains("cmd2"));
            assertTrue(output.contains("cmd3"));
            assertTrue(output.contains("Description 1"));
            assertTrue(output.contains("Description 2"));
            assertTrue(output.contains("Description 3"));
        }

        @Test
        @DisplayName("Should handle empty command list")
        void printHelpWithNoCommands() {
            parser.printHelp();
            String output = outContent.toString();
            assertTrue(output.contains("AVAILABLE COMMANDS"));
        }
    }
}