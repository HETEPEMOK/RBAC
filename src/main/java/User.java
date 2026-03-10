import java.util.regex.Pattern;

public record User(String username, String fullname, String email) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    public User{
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username must not be null or empty");
        if (fullname == null || fullname.isBlank()) throw new IllegalArgumentException("Fullname must not be null or empty");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email must not be null or empty and need valid email format");

        if (!USERNAME_PATTERN.matcher(username).matches()) throw new IllegalArgumentException("Username must be 3-20 cahrs, only latin letters, digits, underscore");
        if (!EMAIL_PATTERN.matcher(email).matches()) throw new IllegalArgumentException("Invalid email format");
    }

    public static User create(String username, String fullname, String email)
    {
        return new User(username, fullname, email);
    }
    public String format()
    {
        return String.format("%s (%s) <%s>", username, fullname, email);
    }
}
