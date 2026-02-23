public class AssignmentFilters {

    public static AssignmentFilter byUser(User user)
    {
        return assignment -> assignment.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username)
    {
        return assignment -> assignment.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role)
    {
        return assignment -> assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName)
    {
        return assignment -> assignment.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly()
    {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly()
    {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type)
    {
        return assignment -> assignment.assignmentType().equals(type);
    }

    public static AssignmentFilter assignedBy(String username)
    {
        return assignment -> assignment.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignmentAfter(String date)
    {
        return assignment -> {
            String assignmentAt = assignment.metadata().assignedAt();
            return assignmentAt.compareTo(date) >= 0;
        };
    }

    public static AssignmentFilter expiringBefore(String date)
    {
        return assignment -> {
            if (assignment instanceof TemporaryAssignment temp)
            {
                return temp.getExpiresAt().compareTo(date) <= 0;
            }
            return false;
        };
    }
}
