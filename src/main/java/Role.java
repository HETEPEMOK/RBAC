import java.util.*;

public class Role {
    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions;
    public Role(String name, String description)
    {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Role name must not be null or empty");
        this.id = "role_" + UUID.randomUUID().toString();
        this.name = name;
        this.description = Objects.requireNonNull(description, "Description must not be null");
        this.permissions = new HashSet<>();
    }

    public String getId() {return id;}
    public String getName() {return name;}
    public String getDescription() {return description;}

    public void addPermission(Permission permission)
    {
        permissions.add(permission);
    }
    public void removePermission(Permission permission)
    {
        permissions.remove(permission);
    }
    public boolean hasPermission(Permission permission)
    {
        return permissions.contains(permission); // rework
    }
    public boolean hasPermission(String permissionName, String resource)
    {
        return permissions.stream().anyMatch(p-> p.matches(permissionName, resource));
    }
    public Set<Permission> getPermissions()
    {
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return id.equals(role.id);
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
    @Override
    public String toString()
    {
        return String.format("Role{id=%s, name=%s}", id, name);
    }
    public String format()
    {
        StringBuilder result = new StringBuilder();
        result.append(String.format("Role: %s [ID: %s]\n", name, id));
        result.append(String.format("Description: %s\n", description));
        result.append(String.format("Permissions (%d):\n", permissions.size()));
        for (Permission p: permissions)
        {
            result.append(" - ").append(p.format()).append("\n");
        }
        return result.toString();
    }
}
