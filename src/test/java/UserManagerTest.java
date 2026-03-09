import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@DisplayName("UserManager Tests")
class UserManagerTest {
    private UserManager userManager;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        user1 = User.create("john_doe", "John Doe", "john@company.com");
        user2 = User.create("jane_smith", "Jane Smith", "jane@company.com");
        user3 = User.create("bob_wilson", "Bob Wilson", "bob@gmail.com");
    }

    @Nested
    @DisplayName("User Addition Tests")
    class AddTests {
        @Test
        @DisplayName("Should successfully add a user")
        void addUserSuccess() {
            userManager.add(user1);
            assertEquals(1, userManager.count());
            assertTrue(userManager.exists("john_doe"));
        }

        @Test
        @DisplayName("Should throw exception when adding null user")
        void addNullUserThrowsException() {
            assertThrows(NullPointerException.class, () -> userManager.add(null));
        }

        @Test
        @DisplayName("Should throw exception when adding user with existing username")
        void addDuplicateUserThrowsException() {
            userManager.add(user1);
            User duplicate = User.create("john_doe", "John Doe Copy", "john.copy@company.com");

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userManager.add(duplicate)
            );
            assertTrue(exception.getMessage().contains("already exists"));
        }
    }

    @Nested
    @DisplayName("User Removal Tests")
    class RemoveTests {
        @BeforeEach
        void setUp() {
            userManager.add(user1);
            userManager.add(user2);
        }

        @Test
        @DisplayName("Should return true when removing existing user")
        void removeExistingUserReturnsTrue() {
            assertTrue(userManager.remove(user1));
            assertEquals(1, userManager.count());
            assertFalse(userManager.exists("john_doe"));
        }

        @Test
        @DisplayName("Should return false when removing non-existing user")
        void removeNonExistingUserReturnsFalse() {
            User nonExisting = User.create("non_existing", "Non Existing", "non@test.com");
            assertFalse(userManager.remove(nonExisting));
            assertEquals(2, userManager.count());
        }

        @Test
        @DisplayName("Should throw exception when removing null user")
        void removeNullThrowsException() {
            assertThrows(NullPointerException.class, () -> userManager.remove(null));
        }
    }

    @Nested
    @DisplayName("User Search Tests")
    class FindTests {
        @BeforeEach
        void setUp() {
            userManager.add(user1);
            userManager.add(user2);
            userManager.add(user3);
        }

        @Test
        @DisplayName("Should find user by existing username")
        void findByUsernameExisting() {
            Optional<User> found = userManager.findByUsername("john_doe");
            assertTrue(found.isPresent());
            assertEquals("john_doe", found.get().username());
            assertEquals("john@company.com", found.get().email());
        }

        @Test
        @DisplayName("Should return Optional.empty() for non-existing username")
        void findByUsernameNonExisting() {
            Optional<User> found = userManager.findByUsername("non_existing");
            assertTrue(found.isEmpty());
        }

        @Test
        @DisplayName("Should find user by existing email")
        void findByEmailExisting() {
            Optional<User> found = userManager.findByEmail("jane@company.com");
            assertTrue(found.isPresent());
            assertEquals("jane_smith", found.get().username());
        }

        @Test
        @DisplayName("Should return Optional.empty() for non-existing email")
        void findByEmailNonExisting() {
            Optional<User> found = userManager.findByEmail("non@existing.com");
            assertTrue(found.isEmpty());
        }

        @Test
        @DisplayName("findAll should return all users")
        void findAllReturnsAllUsers() {
            List<User> all = userManager.findAll();
            assertEquals(3, all.size());
            assertTrue(all.contains(user1));
            assertTrue(all.contains(user2));
            assertTrue(all.contains(user3));
        }
    }

    @Nested
    @DisplayName("User Filtering Tests")
    class FilterTests {
        @BeforeEach
        void setUp() {
            userManager.add(user1);
            userManager.add(user2);
            userManager.add(user3);
        }

        @Test
        @DisplayName("Filter by exact username")
        void filterByExactUsername() {
            List<User> result = userManager.findByFilter(UserFilters.byUsername("john_doe"));
            assertEquals(1, result.size());
            assertEquals("john_doe", result.get(0).username());
        }

        @Test
        @DisplayName("Filter by username substring (case insensitive)")
        void filterByUsernameContains() {
            List<User> result = userManager.findByFilter(UserFilters.byUsernameContains("JOHN"));
            assertEquals(1, result.size());
            assertEquals("john_doe", result.get(0).username());
        }

        @Test
        @DisplayName("Filter by email domain")
        void filterByEmailDomain() {
            List<User> result = userManager.findByFilter(UserFilters.byEmailDomain("@company.com"));
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(u -> u.email().endsWith("@company.com")));
        }

        @Test
        @DisplayName("Filter by full name substring")
        void filterByFullNameContains() {
            List<User> result = userManager.findByFilter(UserFilters.byFullNameContains("Smith"));
            assertEquals(1, result.size());
            assertEquals("jane_smith", result.get(0).username());
        }

        @Test
        @DisplayName("Combined AND filter")
        void combinedAndFilter() {
            UserFilter filter = UserFilters.byEmailDomain("@company.com")
                    .and(UserFilters.byFullNameContains("John"));

            List<User> result = userManager.findByFilter(filter);
            assertEquals(1, result.size());
            assertEquals("john_doe", result.get(0).username());
        }

        @Test
        @DisplayName("Combined OR filter")
        void combinedOrFilter() {
            UserFilter filter = UserFilters.byUsername("john_doe")
                    .or(UserFilters.byUsername("bob_wilson"));

            List<User> result = userManager.findByFilter(filter);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("findAll with filter and sorting")
        void findAllWithFilterAndSorter() {
            List<User> result = userManager.findAll(
                    UserFilters.byEmailDomain("@company.com"),
                    UserSorters.byUsername()
            );

            assertEquals(2, result.size());
            assertEquals("jane_smith", result.get(0).username());
            assertEquals("john_doe", result.get(1).username());
        }

        @Test
        @DisplayName("Should throw exception when filter is null")
        void nullFilterThrowsException() {
            assertThrows(NullPointerException.class, () -> userManager.findByFilter(null));
        }
    }

    @Nested
    @DisplayName("User Update Tests")
    class UpdateTests {
        @BeforeEach
        void setUp() {
            userManager.add(user1);
        }

        @Test
        @DisplayName("Should update existing user")
        void updateExistingUser() {
            userManager.update("john_doe", "John Updated", "john.updated@company.com");

            Optional<User> updated = userManager.findByUsername("john_doe");
            assertTrue(updated.isPresent());
            assertEquals("John Updated", updated.get().fullname());
            assertEquals("john.updated@company.com", updated.get().email());
        }

        @Test
        @DisplayName("Should throw exception when updating non-existing user")
        void updateNonExistingUserThrowsException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userManager.update("non_existing", "Name", "email@test.com")
            );
            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Should throw exception when updating with invalid data")
        void updateWithInvalidDataThrowsException() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> userManager.update("john_doe", "", "email@test.com")
            );
        }
    }

    @Nested
    @DisplayName("Helper Methods Tests")
    class HelperMethodsTests {
        @Test
        @DisplayName("count should return correct number of users")
        void countReturnsCorrectNumber() {
            assertEquals(0, userManager.count());
            userManager.add(user1);
            assertEquals(1, userManager.count());
            userManager.add(user2);
            assertEquals(2, userManager.count());
        }

        @Test
        @DisplayName("exists should correctly check user existence")
        void existsReturnsCorrectValue() {
            userManager.add(user1);
            assertTrue(userManager.exists("john_doe"));
            assertFalse(userManager.exists("non_existing"));
        }

        @Test
        @DisplayName("clear should remove all users")
        void clearRemovesAllUsers() {
            userManager.add(user1);
            userManager.add(user2);
            assertEquals(2, userManager.count());

            userManager.clear();
            assertEquals(0, userManager.count());
            assertFalse(userManager.exists("john_doe"));
        }
    }

    @Nested
    @DisplayName("Complex Filtering Methods Tests")
    class ComplexFilterTests {
        @BeforeEach
        void setUp() {
            userManager.add(user1);
            userManager.add(user2);
            userManager.add(user3);
            userManager.add(User.create("admin", "Admin User", "admin@company.com"));
            userManager.add(User.create("guest", "Guest User", "guest@gmail.com"));
        }

        @Test
        @DisplayName("findUsersByMultipleCriteria with multiple conditions")
        void findUsersByMultipleCriteria() {
            List<User> result = userManager.findUserByMultipleCriteria(
                    "john", "@company.com", "John"
            );

            assertEquals(1, result.size());
            assertEquals("john_doe", result.get(0).username());
        }

        @Test
        @DisplayName("findUsersByMultipleCriteria with incompatible conditions")
        void findUsersByMultipleCriteriaNoMatch() {
            List<User> result = userManager.findUserByMultipleCriteria(
                    "jane", "@gmail.com", "Jane"
            );

            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("findUsersByUsernameOrEmail with username only")
        void findUsersByUsernameOrEmailWithUsername() {
            List<User> result = userManager.findUsersByUsernameOrEmail("john_doe", "");
            assertEquals(1, result.size());
            assertEquals("john_doe", result.get(0).username());
        }

        @Test
        @DisplayName("findUsersByUsernameOrEmail with email only")
        void findUsersByUsernameOrEmailWithEmail() {
            List<User> result = userManager.findUsersByUsernameOrEmail("", "bob@gmail.com");
            assertEquals(1, result.size());
            assertEquals("bob_wilson", result.get(0).username());
        }

        @Test
        @DisplayName("findUsersByUsernameOrEmail with both conditions (OR)")
        void findUsersByUsernameOrEmailWithBoth() {
            List<User> result = userManager.findUsersByUsernameOrEmail("john_doe", "bob@gmail.com");
            assertEquals(2, result.size());
        }
    }
}