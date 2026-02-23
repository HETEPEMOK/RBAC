import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> usersByUsername = new HashMap<>();

    @Override
    public void add(User user)
    {
        Objects.requireNonNull(user, "User cannot be null");
        String username = user.username();
        if (usersByUsername.containsKey(username))
        {
            throw new IllegalArgumentException("User witch username " + username + " already exists");
        }
        usersByUsername.put(username, user);
    }

    @Override
    public boolean remove(User user)
    {
        Objects.requireNonNull(user, "User cannot be null");
        return usersByUsername.remove(user.username()) != null;
    }

    @Override
    public Optional<User> findById(String id)
    {
        return Optional.ofNullable(usersByUsername.get(id));
    }

    @Override
    public List<User> findAll()
    {
        return new ArrayList<>(usersByUsername.values());
    }

    @Override
    public int count()
    {
        return usersByUsername.size();
    }

    @Override
    public void clear()
    {
        usersByUsername.clear();
    }

    public Optional<User> findByUsername(String username)
    {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    public Optional<User> findByEmail(String email)
    {
        UserFilter filter = UserFilters.byEmail(email);
        return usersByUsername.values().stream().filter(filter::test).findFirst();
    }

    public List<User> findByFilter(UserFilter filter)
    {
        Objects.requireNonNull(filter, "Filter cannot be null");
        return usersByUsername.values().stream().filter(filter::test).collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter)
    {
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(sorter, "Comparator cannot be null");
        return usersByUsername.values().stream().filter(filter::test).sorted(sorter).collect(Collectors.toList());
    }

    public void update(String username, String newFullName, String newEmail)
    {
        User existing = usersByUsername.get(username);
        if (existing == null)
        {
            throw new IllegalArgumentException("User not found: " + username);
        }

        User updated = User.create(username, newFullName, newEmail);
        usersByUsername.put(username, updated);
    }

    public List<User> findUserByMultipleCriteria(String usernamePart, String emailDomain, String namePart)
    {
        UserFilter filter = UserFilters.byUsernameContains(usernamePart).and(UserFilters.byEmailDomain(emailDomain)).and(UserFilters.byFullNameContains(namePart));
        return findByFilter(filter);
    }

    public List<User> findUsersByUsernameOrEmail(String username, String email)
    {
        UserFilter filter = UserFilters.byUsername(username).or(UserFilters.byEmail(email));
        return findByFilter(filter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserManager that)) return false;
        return Objects.equals(usersByUsername, that.usersByUsername);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(usersByUsername);
    }
}
