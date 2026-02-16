import java.util.Locale;
import java.util.regex.Pattern;

public record Permission(String name, String resource, String description) {
    public Permission
    {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Permission name must not be null or empty");
        if (resource == null || resource.trim().isEmpty()) throw new IllegalArgumentException("Permission resource must not be null or empty");
        if (description == null || description.trim().isEmpty()) throw new IllegalArgumentException("Permission description must not be null or empty");
        if (name.contains(" ")) throw new IllegalArgumentException("Permission name must not be contain spaces");
        name = name.trim().toUpperCase();
        resource = resource.trim().toLowerCase();
        description = description.trim();
    }
    String format()
    {
        return String.format("%s on %s: %s", name, resource, description);
    }
    boolean matches(String namePattern, String resourcePattern)
    {
        boolean nameMatches = (namePattern == null) || this.name.contains(namePattern.toUpperCase());
        boolean resourceMatches = (resourcePattern == null) || this.resource.contains(resourcePattern.toLowerCase());
        return nameMatches && resourceMatches;
    }
}
