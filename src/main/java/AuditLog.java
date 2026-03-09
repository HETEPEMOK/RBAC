import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog
{
    private final List<AuditEntry> entries = new ArrayList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public void log(String action, String performer, String target, String details)
    {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        entries.add(entry);
    }

    public List<AuditEntry> getAll()
    {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer)
    {
        return entries.stream()
                .filter(e -> e.performer().equals(performer))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action)
    {
        return entries.stream()
                .filter(e -> e.action().equals(action))
                .collect(Collectors.toList());
    }

    public void printLog()
    {
        if (entries.isEmpty())
        {
            System.out.println("No audit log entries.");
            return;
        }

        System.out.println("\n=== AUDIT LOG ===");
        for (AuditEntry entry: entries)
        {
            System.out.println(entry);
        }
        System.out.println("Total entries: " + entries.size());
    }

    public void saveToFile(String filename)
    {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename)))
        {
            for (AuditEntry entry: entries)
            {
                writer.println(entry);
            }
            System.out.println("Audit log saved to: " + filename);
        } catch (IOException e)
        {
            System.err.println("Error saving audit log: " + e.getMessage());
        }
    }

    public void clear()
    {
        entries.clear();
    }

    public int count()
    {
        return entries.size();
    }
}