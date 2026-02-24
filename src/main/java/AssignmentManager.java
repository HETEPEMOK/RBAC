import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignmentsById = new HashMap<>();
    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment assignment) {
        Objects.requireNonNull(assignment, "Assignment cannot be null");

        if (!userManager.exists(assignment.user().username())) {
            throw new IllegalArgumentException("User does not exist: " + assignment.user().username());
        }
        if (!roleManager.exists(assignment.role().getName())) {
            throw new IllegalArgumentException("Role does not exist: " + assignment.role().getName());
        }

        if (hasActiveAssignment(assignment.user(), assignment.role())) {
            throw new IllegalArgumentException("User already has active assignment for this role");
        }

        assignmentsById.put(assignment.assignmentId(), assignment);
    }

    private boolean hasActiveAssignment(User user, Role role) {
        AssignmentFilter filter = AssignmentFilters.byUser(user)
                .and(AssignmentFilters.byRole(role))
                .and(AssignmentFilters.activeOnly());

        return !findByFilter(filter).isEmpty();
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        return assignmentsById.remove(assignment.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignmentsById.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignmentsById.values());
    }

    @Override
    public int count() {
        return assignmentsById.size();
    }

    @Override
    public void clear() {
        assignmentsById.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        return findByFilter(AssignmentFilters.byUser(user));
    }

    public List<RoleAssignment> findByRole(Role role) {
        return findByFilter(AssignmentFilters.byRole(role));
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        return assignmentsById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(sorter, "Comparator cannot be null");
        return assignmentsById.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return findByFilter(AssignmentFilters.activeOnly());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return findByFilter(AssignmentFilters.inactiveOnly());
    }

    public List<RoleAssignment> getPermanentAssignments() {
        return findByFilter(AssignmentFilters.byType("PERMANENT"));
    }

    public List<RoleAssignment> getTemporaryAssignments() {
        return findByFilter(AssignmentFilters.byType("TEMPORARY"));
    }

    public List<RoleAssignment> getAssignmentsByAssigner(String username) {
        return findByFilter(AssignmentFilters.assignedBy(username));
    }

    public List<RoleAssignment> getAssignmentsAfterDate(String date) {
        return findByFilter(AssignmentFilters.assignmentAfter(date));
    }

    public List<RoleAssignment> getExpiringBeforeDate(String date) {
        return findByFilter(AssignmentFilters.expiringBefore(date));
    }

    public boolean userHasRole(User user, Role role) {
        AssignmentFilter filter = AssignmentFilters.byUser(user)
                .and(AssignmentFilters.byRole(role))
                .and(AssignmentFilters.activeOnly());

        return !findByFilter(filter).isEmpty();
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return getUserPermissions(user).stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(permissionName) &&
                        p.resource().equalsIgnoreCase(resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        return findByFilter(AssignmentFilters.byUser(user).and(AssignmentFilters.activeOnly()))
                .stream()
                .map(RoleAssignment::role)
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignmentsById.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment not found: " + assignmentId);
        }

        if (assignment instanceof PermanentAssignment perm) {
            perm.revoke();
        } else {
            assignmentsById.remove(assignmentId);
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = assignmentsById.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment not found: " + assignmentId);
        }

        if (!(assignment instanceof TemporaryAssignment temp)) {
            throw new IllegalArgumentException("Assignment is not temporary");
        }

        temp.extend(newExpirationDate);
    }

    public List<RoleAssignment> findComplexAssignments(String username, String roleName,
                                                       String assigner, boolean onlyActive) {
        AssignmentFilter filter = AssignmentFilters.byUsername(username)
                .and(AssignmentFilters.byRoleName(roleName));

        if (assigner != null && !assigner.isBlank()) {
            filter = filter.and(AssignmentFilters.assignedBy(assigner));
        }

        if (onlyActive) {
            filter = filter.and(AssignmentFilters.activeOnly());
        }

        return findByFilter(filter);
    }

    public Map<String, Long> getAssignmentStatisticsByType() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("PERMANENT", (long) getPermanentAssignments().size());
        stats.put("TEMPORARY", (long) getTemporaryAssignments().size());
        stats.put("ACTIVE", (long) getActiveAssignments().size());
        stats.put("EXPIRED", (long) getExpiredAssignments().size());
        return stats;
    }
}