import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RBACSystem Tests")
class RBACSystemTest {
    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {
        @Test
        @DisplayName("Should initialize with default data")
        void initialize() {
            system.initialize();

            // Check that managers are not null
            assertNotNull(system.getUserManager());
            assertNotNull(system.getRoleManager());
            assertNotNull(system.getAssignmentManager());

            // Check that admin user was created
            assertTrue(system.getUserManager().exists("admin"), "Admin user should exist");

            // Check that default roles were created
            assertTrue(system.getRoleManager().exists("Admin"), "Admin role should exist");
            assertTrue(system.getRoleManager().exists("Manager"), "Manager role should exist");
            assertTrue(system.getRoleManager().exists("Viewer"), "Viewer role should exist");

            // Check that admin has role assigned
            var adminOpt = system.getUserManager().findByUsername("admin");
            assertTrue(adminOpt.isPresent(), "Admin user should be present");

            var assignments = system.getAssignmentManager().findByUser(adminOpt.get());
            assertFalse(assignments.isEmpty(), "Admin should have at least one assignment");

            // Admin should have Admin role
            boolean hasAdminRole = assignments.stream()
                    .anyMatch(a -> a.role().getName().equals("Admin"));
            assertTrue(hasAdminRole, "Admin should have Admin role");
        }

        @Test
        @DisplayName("Should set current user to admin after initialization")
        void currentUserAfterInit() {
            system.initialize();
            assertEquals("admin", system.getCurrentUsr(), "Current user should be admin");
        }
    }

    @Nested
    @DisplayName("Current User Tests")
    class CurrentUserTests {
        @Test
        @DisplayName("Should set current user if user exists")
        void setCurrentUserExisting() {
            system.initialize();
            system.setCurrentUsr("admin");
            assertEquals("admin", system.getCurrentUsr());
        }

        @Test
        @DisplayName("Should throw exception when setting non-existent user")
        void setCurrentUserNonExisting() {
            system.initialize();
            assertThrows(IllegalArgumentException.class,
                    () -> system.setCurrentUsr("nonexistent"),
                    "Should throw exception for non-existent user");
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {
        @Test
        @DisplayName("Should generate statistics with initialized system")
        void generateStatistics() {
            system.initialize();
            String stats = system.generateStatistics();

            assertNotNull(stats);

            // Check for the actual header from the implementation
            assertTrue(stats.contains("SYSTEM STATISTIC"),
                    "Should contain statistics header: 'SYSTEM STATISTIC'");

            // Check for expected content
            assertTrue(stats.contains("Users: 1"), "Should show 1 user");
            assertTrue(stats.contains("Roles: 3"), "Should show 3 roles");
            assertTrue(stats.contains("Total Assignments: 1"), "Should show 1 assignment");
            assertTrue(stats.contains("Active:"), "Should show active assignments count");
            assertTrue(stats.contains("Expired/Inactive:"), "Should show expired/inactive count");
        }

        @Test
        @DisplayName("Should generate statistics with empty system")
        void generateStatisticsEmpty() {
            String stats = system.generateStatistics();

            assertNotNull(stats);

            // Check for the actual header from the implementation
            assertTrue(stats.contains("SYSTEM STATISTIC"),
                    "Should contain statistics header: 'SYSTEM STATISTIC'");

            // Check for zero counts
            assertTrue(stats.contains("Users: 0"), "Should show 0 users");
            assertTrue(stats.contains("Roles: 0"), "Should show 0 roles");
            assertTrue(stats.contains("Total Assignments: 0"), "Should show 0 assignments");
        }

        @Test
        @DisplayName("Should show active and expired counts")
        void showActiveExpiredCounts() {
            system.initialize();

            // Add another user with temporary assignment
            User user = User.create("testuser", "Test User", "test@test.com");
            system.getUserManager().add(user);

            Role viewerRole = system.getRoleManager().findByName("Viewer").get();
            AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test");

            // Add temporary assignment that expires in the future
            String futureDate = "2030-12-31 23:59";
            TemporaryAssignment tempAssign = new TemporaryAssignment(
                    user, viewerRole, metadata, futureDate, false
            );
            system.getAssignmentManager().add(tempAssign);

            String stats = system.generateStatistics();

            assertTrue(stats.contains("Active:"), "Should show active count");
            assertTrue(stats.contains("Expired/Inactive:"), "Should show expired/inactive count");
        }
    }

    @Nested
    @DisplayName("Manager Getters Tests")
    class ManagerGettersTests {
        @Test
        @DisplayName("Should return non-null managers")
        void managersNotNull() {
            assertNotNull(system.getUserManager());
            assertNotNull(system.getRoleManager());
            assertNotNull(system.getAssignmentManager());
        }

        @Test
        @DisplayName("Should return same manager instances")
        void managerInstances() {
            UserManager um1 = system.getUserManager();
            UserManager um2 = system.getUserManager();
            assertSame(um1, um2, "Should return same UserManager instance");

            RoleManager rm1 = system.getRoleManager();
            RoleManager rm2 = system.getRoleManager();
            assertSame(rm1, rm2, "Should return same RoleManager instance");

            AssignmentManager am1 = system.getAssignmentManager();
            AssignmentManager am2 = system.getAssignmentManager();
            assertSame(am1, am2, "Should return same AssignmentManager instance");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Should handle initialize called multiple times")
        void initializeMultipleTimes() {
            system.initialize();
            int userCount = system.getUserManager().count();
            int roleCount = system.getRoleManager().count();

            // Initialize again - should not duplicate data
            system.initialize();

            assertEquals(userCount, system.getUserManager().count(),
                    "User count should not change on second initialize");
            assertEquals(roleCount, system.getRoleManager().count(),
                    "Role count should not change on second initialize");
        }

        @Test
        @DisplayName("Should handle getCurrentUser before initialization")
        void getCurrentUserBeforeInit() {
            assertEquals("system", system.getCurrentUsr(),
                    "Default current user should be 'system'");
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        @Test
        @DisplayName("Should create complete workflow: user -> role -> assignment")
        void completeWorkflow() {
            system.initialize();

            // Create new user
            User newUser = User.create("newuser", "New User", "new@test.com");
            system.getUserManager().add(newUser);

            // Get viewer role
            Role viewerRole = system.getRoleManager().findByName("Viewer").get();

            // Assign role
            AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Integration test");
            PermanentAssignment assignment = new PermanentAssignment(newUser, viewerRole, metadata);
            system.getAssignmentManager().add(assignment);

            // Verify
            assertTrue(system.getAssignmentManager().userHasRole(newUser, viewerRole));

            // Check permissions
            assertTrue(system.getAssignmentManager().userHasPermission(newUser, "READ", "users"));

            // Generate stats
            String stats = system.generateStatistics();
            assertNotNull(stats);
            assertTrue(stats.contains("Users: 2"), "Should show 2 users");
        }
    }
}