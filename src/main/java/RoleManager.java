import java.util.*;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new HashMap<>();
    private final Map<String, Role> rolesByName = new HashMap<>();
    private AssignmentManager assignmentManager;

    public RoleManager() {
    }

    public void setAssignmentManager(AssignmentManager assignmentManager) {
        this.assignmentManager = assignmentManager;
    }

    @Override
    public void add(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        if (rolesByName.containsKey(role.getName())) {
            throw new IllegalArgumentException("Role with name " + role.getName() + " already exists");
        }
        rolesById.put(role.getId(), role);
        rolesByName.put(role.getName(), role);
    }

    @Override
    public boolean remove(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        if (isRoleAssigned(role)) {
            throw new IllegalStateException("Cannot delete role that is assigned to users");
        }
        Role removed = rolesById.remove(role.getId());
        if (removed != null) {
            rolesByName.remove(removed.getName());
            return true;
        }
        return false;
    }

    private boolean isRoleAssigned(Role role) {
        if (assignmentManager != null) {
            return !assignmentManager.findByRole(role).isEmpty();
        }
        return false;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(sorter, "Comparator cannot be null");
        return rolesById.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        RoleFilter filter = RoleFilters.hasPermission(permissionName, resource);
        return findByFilter(filter);
    }

    public List<Role> findRolesByNameAndMinPermissions(String namePart, int minPermissions) {
        RoleFilter filter = RoleFilters.byNameContains(namePart)
                .and(RoleFilters.hasAtLeastNPermissions(minPermissions));

        return findByFilter(filter);
    }

    public List<Role> findRolesWithEitherPermission(Permission perm1, Permission perm2) {
        RoleFilter filter = RoleFilters.hasPermission(perm1)
                .or(RoleFilters.hasPermission(perm2));

        return findByFilter(filter);
    }
}