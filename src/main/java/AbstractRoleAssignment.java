import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment{
    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata)
    {
        this.assignmentId = "assignment_" + UUID.randomUUID().toString();
        this.user = Objects.requireNonNull(user, "User must not be null");
        this.role = Objects.requireNonNull(role, "Role must not be null");
        this.metadata = Objects.requireNonNull(metadata, "Metadata must not be null");
    }

    @Override
    public String assignmentId() {return assignmentId;}

    @Override
    public User user() {return user;}

    @Override
    public Role role() {return role;}

    @Override
    public AssignmentMetadata metadata() {return metadata;}

    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractRoleAssignment that)) return false;
        return assignmentId.equals(that.assignmentId);
    }

    @Override
    public int hashCode() {return Objects.hash(assignmentId);}

    public String summary()
    {
        String status = isActive() ? "ACTIVE": "INACTIVE";
        String reason = metadata().reason() == null ? "N/A": metadata().reason();
        return String.format("[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s", assignmentType(), role().getName(), user().username(), metadata().assignedBy(), metadata().assignedAt(), reason, status);
    }
}
