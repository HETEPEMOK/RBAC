import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment{
    private String expiresAt;
    private boolean autoRenew;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew)
    {
        super(user, role, metadata);
        setExpiresAt(expiresAt);
        this.autoRenew = autoRenew;
    }

    private void setExpiresAt(String expiresAt)
    {
        if (expiresAt == null || expiresAt.isBlank()) {throw new IllegalArgumentException("ExpiresAt must not be empty");}
        try{
            LocalDateTime.parse(expiresAt, TIME_FORMAT);
        } catch (Exception e)
        {
            throw new IllegalArgumentException("ExpiresAt must be in format yyyy-MM-dd HH:mm");
        }
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean isActive() {return !isExpired();}

    @Override
    public String assignmentType(){return "TEMPORARY";}

    public boolean isExpired()
    {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = LocalDateTime.parse(expiresAt, TIME_FORMAT);
        return now.isAfter(expiration);
    }

    public void extend(String newExpirationDate)
    {
        setExpiresAt(newExpirationDate);
    }

    public String getExpiresAt() {return expiresAt;}
    public boolean isAutoRenew() {return autoRenew;}

    public String getTimeRemaining()
    {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = LocalDateTime.parse(expiresAt, TIME_FORMAT);
        if (now.isAfter(expiration)) return "Expired";
        long days = ChronoUnit.DAYS.between(now, expiration);
        long hours = ChronoUnit.HOURS.between(now, expiration) % 24;
        long minutes = ChronoUnit.MINUTES.between(now, expiration) % (24 * 60);
        long seconds = ChronoUnit.SECONDS.between(now,expiration) % (24 * 60 * 60);
        return String.format("%d %d:%d:%d", days, hours, minutes, seconds);
    }

    @Override
    public String summary()
    {
        return super.summary() + String.format("\nExpires at: %s (Auto-renew: %s)", expiresAt, autoRenew);
    }
}
