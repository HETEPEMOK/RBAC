public record AuditEntry(String timestamp, String action, String performer, String target, String details)
{
    @Override
    public String toString()
    {
        return String.format("[%s] %s | Performer: %s | Target: %s | Details: %s",
                timestamp, action, performer, target, details);
    }
}