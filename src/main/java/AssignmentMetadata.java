import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AssignmentMetadata{
        if (assignedBy == null || assignedBy.isBlank()) throw new IllegalArgumentException("assignedBy must not be empty");
        if (assignedAt == null || assignedAt.isBlank()) throw new IllegalArgumentException("assignedAt must not be empty");

    }
    public static AssignmentMetadata now(String assignedBy, String reason)
    {
        String now = LocalDateTime.now().format(TIME_FORMAT);
        return new AssignmentMetadata(assignedBy, now, reason);
    }

    public String format()
    {
        if (reason == null || reason.isBlank())
        {
            return String.format("Assigned by %s at %s", assignedBy, assignedAt);
        }
        return String.format("Assigned by %s at %s (reason: %s)", assignedBy, assignedAt, reason);
    }
}
