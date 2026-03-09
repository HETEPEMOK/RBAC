import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Map<String, String> commandDescription = new LinkedHashMap<>();

    public void registerCommand(String name, String description, Command command)
    {
        commands.put(name.toLowerCase(), command);
        commandDescription.put(name.toLowerCase(), description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system)
    {
        Command command = commands.get(commandName.toLowerCase());
        if (command != null)
        {
            try
            {
                command.execute(scanner, system);
            } catch (Exception e)
            {
                System.err.println("Error executing command: " + e.getMessage());
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("Unknown command: '" + commandName + "'. Type 'help' for available commands.");
        }
    }

    public void printHelp(){
        System.out.println("\n=========== AVAILABLE COMMANDS ===========");
        int maxCmdLength = commandDescription.keySet().stream().mapToInt(String::length).max().orElse(11) + 2;
        String format = " %-" + maxCmdLength + "s - %s%n";
        commandDescription.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> System.out.printf(format, entry.getKey(), entry.getValue()));
        System.out.println("==========================================\n");
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system)
    {
        if (input == null || input.trim().isEmpty())
        {
            return;
        }

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();

        executeCommand(commandName, scanner, system);
    }

    public Map<String, Command> getCommands() {return commands;}
}