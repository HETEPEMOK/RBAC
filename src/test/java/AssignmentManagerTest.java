import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;

@DisplayName("AssignmentManager Tests")
class AssignmentManagerTest {
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private User user1;
    private User user2;
    private User user3;
    private Role adminRole;
    private Role viewerRole;
    private Role editorRole;
    private Permission readUsers;
    private Permission writeUsers;
    private Permission deleteUsers;
    private AssignmentMetadata metadata;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        roleManager.setAssignmentManager(assignmentManager);

        // Create users
        user1 = User.create("john_doe", "John Doe", "john@company.com");
        user2 = User.create("jane_smith", "Jane Smith", "jane@company.com");
        user3 = User.create("bob_wilson", "Bob Wilson", "bob@gmail.com");
        userManager.add(user1);
        userManager.add(user2);
        userManager.add(user3);

        // Create permissions
        readUsers = new Permission("READ", "users", "Can read users");
        writeUsers = new Permission("WRITE", "users", "Can write users");
        deleteUsers = new Permission("DELETE", "users", "Can delete users");

        // Create roles
        adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);

        viewerRole = new Role("Viewer", "View only");
        viewerRole.addPermission(readUsers);

        editorRole = new Role("Editor", "Can edit content");
        editorRole.addPermission(readUsers);
        editorRole.addPermission(writeUsers);

        roleManager.add(adminRole);
        roleManager.add(viewerRole);
        roleManager.add(editorRole);

        // Metadata for assignments
        metadata = AssignmentMetadata.now("admin", "Test assignment");
    }

    @Nested
    @DisplayName("Assignment Addition Tests")
    class AddTests {
        @Test
        @DisplayName("Should successfully add permanent assignment")
        void addPermanentAssignmentSuccess() {
            PermanentAssignment assignment = new PermanentAssignment(user1, adminRole, metadata);
            assignmentManager.add(assignment);

            assertEquals(1, assignmentManager.count());
            assertTrue(assignmentManager.userHasRole(user1, adminRole));
        }

        @Test
        @DisplayName("Should successfully add temporary assignment")
        void addTemporaryAssignmentSuccess() {
            TemporaryAssignment assignment = new TemporaryAssignment(
                    user1, viewerRole, metadata, "2026-12-31 23:59", false
            );
            assignmentManager.add(assignment);

            assertEquals(1, assignmentManager.count());
            assertTrue(assignmentManager.userHasRole(user1, viewerRole));
        }

        @Test
        @DisplayName("Should throw exception when adding null assignment")
        void addNullAssignmentThrowsException() {
            assertThrows(NullPointerException.class, () -> assignmentManager.add(null));
        }

        @Test
        @DisplayName("Should throw exception when adding assignment with non-existing user")
        void addAssignmentWithNonExistingUserThrowsException() {
            User nonExisting = User.create("non_existing", "Non Existing", "non@test.com");
            PermanentAssignment assignment = new PermanentAssignment(nonExisting, adminRole, metadata);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> assignmentManager.add(assignment)
            );
            assertTrue(exception.getMessage().contains("User does not exist"));
        }

        @Test
        @DisplayName("Should throw exception when adding assignment with non-existing role")
        void addAssignmentWithNonExistingRoleThrowsException() {
            Role nonExisting = new Role("NonExisting", "Non existing role");
            PermanentAssignment assignment = new PermanentAssignment(user1, nonExisting, metadata);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> assignmentManager.add(assignment)
            );
            assertTrue(exception.getMessage().contains("Role does not exist"));
        }

        @Test
        @DisplayName("Should throw exception when adding duplicate active assignment")
        void addDuplicateActiveAssignmentThrowsException() {
            PermanentAssignment assignment1 = new PermanentAssignment(user1, adminRole, metadata);
            assignmentManager.add(assignment1);

            PermanentAssignment assignment2 = new PermanentAssignment(user1, adminRole, metadata);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> assignmentManager.add(assignment2)
            );
            assertTrue(exception.getMessage().contains("already has active assignment"));
        }

        @Test
        @DisplayName("Should allow adding different roles to same user")
        void addDifferentRolesToSameUser() {
            PermanentAssignment assignment1 = new PermanentAssignment(user1, adminRole, metadata);
            PermanentAssignment assignment2 = new PermanentAssignment(user1, viewerRole, metadata);

            assignmentManager.add(assignment1);
            assignmentManager.add(assignment2);

            assertEquals(2, assignmentManager.count());
            assertTrue(assignmentManager.userHasRole(user1, adminRole));
            assertTrue(assignmentManager.userHasRole(user1, viewerRole));
        }
    }

    @Nested
    @DisplayName("Assignment Removal Tests")
    class RemoveTests {
        private PermanentAssignment permAssign;
        private TemporaryAssignment tempAssign;

        @BeforeEach
        void setUp() {
            permAssign = new PermanentAssignment(user1, adminRole, metadata);
            tempAssign = new TemporaryAssignment(
                    user2, viewerRole, metadata, "2026-12-31 23:59", false
            );
            assignmentManager.add(permAssign);
            assignmentManager.add(tempAssign);
        }

        @Test
        @DisplayName("Should return true when removing existing assignment")
        void removeExistingAssignmentReturnsTrue() {
            assertTrue(assignmentManager.remove(permAssign));
            assertEquals(1, assignmentManager.count());
            assertFalse(assignmentManager.userHasRole(user1, adminRole));
        }

        @Test
        @DisplayName("Should return false when removing non-existing assignment")
        void removeNonExistingAssignmentReturnsFalse() {
            PermanentAssignment nonExisting = new PermanentAssignment(user3, viewerRole, metadata);
            assertFalse(assignmentManager.remove(nonExisting));
            assertEquals(2, assignmentManager.count());
        }

        @Test
        @DisplayName("Should throw exception when removing null assignment")
        void removeNullThrowsException() {
            assertThrows(NullPointerException.class, () -> assignmentManager.remove(null));
        }
    }

    @Nested
    @DisplayName("Assignment Filtering Tests")
    class FilterTests {
        private PermanentAssignment permAssign;
        private TemporaryAssignment tempAssign;
        private PermanentAssignment permAssign2;

        @BeforeEach
        void setUp() {
            permAssign = new PermanentAssignment(user1, adminRole, metadata);
            tempAssign = new TemporaryAssignment(
                    user2, viewerRole,
                    AssignmentMetadata.now("manager", "Temporary access"),
                    "2026-12-31 23:59", true
            );
            permAssign2 = new PermanentAssignment(user3, editorRole,
                    AssignmentMetadata.now("admin", "Editor access"));

            assignmentManager.add(permAssign);
            assignmentManager.add(tempAssign);
            assignmentManager.add(permAssign2);
        }

        @Test
        @DisplayName("Filter by user")
        void filterByUser() {
            List<RoleAssignment> result = assignmentManager.findByFilter(
                    AssignmentFilters.byUser(user1)
            );
            assertEquals(1, result.size());
            assertEquals(permAssign.assignmentId(), result.get(0).assignmentId());
        }

        @Test
        @DisplayName("Filter by username")
        void filterByUsername() {
            List<RoleAssignment> result = assignmentManager.findByFilter(
                    AssignmentFilters.byUsername("jane_smith")
            );
            assertEquals(1, result.size());
            assertEquals(tempAssign.assignmentId(), result.get(0).assignmentId());
        }

        @Test
        @DisplayName("Filter by role")
        void filterByRole() {
            List<RoleAssignment> result = assignmentManager.findByFilter(
                    AssignmentFilters.byRole(adminRole)
            );
            assertEquals(1, result.size());
            assertEquals(permAssign.assignmentId(), result.get(0).assignmentId());
        }

        @Test
        @DisplayName("Filter by role name")
        void filterByRoleName() {
            List<RoleAssignment> result = assignmentManager.findByFilter(
                    AssignmentFilters.byRoleName("Viewer")
            );
            assertEquals(1, result.size());
            assertEquals(tempAssign.assignmentId(), result.get(0).assignmentId());
        }

        @Test
        @DisplayName("Filter active assignments")
        void filterActiveOnly() {
            List<RoleAssignment> result = assignmentManager.findByFilter(
                    AssignmentFilters.activeOnly()
            );
            assertEquals(3, result.size()); // all are active
        }

        @Test
        @DisplayName("Filter by PERMANENT type")
        void filterByPermanentType() {
            List<RoleAssignment> result = assignmentManager.findByFilter(
                    AssignmentFilters.byType("PERMANENT")
            );
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(a -> a instanceof PermanentAssignment));
        }

        @Test
        @DisplayName("Filter by TEMPORARY type")
        void filterByTemporaryType() {
            List<RoleAssignment> result = assignmentManager.findByFilter(
                    AssignmentFilters.byType("TEMPORARY")
            );
            assertEquals(1, result.size());
            assertTrue(result.get(0) instanceof TemporaryAssignment);
        }

        @Test
        @DisplayName("Filter by assigner")
        void filterByAssignedBy() {
            List<RoleAssignment> result = assignmentManager.findByFilter(
                    AssignmentFilters.assignedBy("manager")
            );
            assertEquals(1, result.size());
            assertEquals(tempAssign.assignmentId(), result.get(0).assignmentId());
        }

        @Test
        @DisplayName("Filter by assignment date")
        void filterByAssignedAfter() {
            List<RoleAssignment> result = assignmentManager.findByFilter(
                    AssignmentFilters.assignmentAfter("2020-01-01 00:00")
            );
            assertEquals(3, result.size()); // all assignments after this date
        }

        @Test
        @DisplayName("Filter by expiration date")
        void filterByExpiringBefore() {
            List<RoleAssignment> result = assignmentManager.findByFilter(
                    AssignmentFilters.expiringBefore("2030-01-01 00:00")
            );
            assertEquals(1, result.size()); // temporary assignment
            assertTrue(result.get(0) instanceof TemporaryAssignment);
        }

        @Test
        @DisplayName("Combined AND filter")
        void combinedAndFilter() {
            AssignmentFilter filter = AssignmentFilters.byType("TEMPORARY")
                    .and(AssignmentFilters.assignedBy("manager"));

            List<RoleAssignment> result = assignmentManager.findByFilter(filter);
            assertEquals(1, result.size());
            assertEquals(tempAssign.assignmentId(), result.get(0).assignmentId());
        }

        @Test
        @DisplayName("findAll with filter and sorting")
        void findAllWithFilterAndSorter() {
            List<RoleAssignment> result = assignmentManager.findAll(
                    AssignmentFilters.byType("PERMANENT"),
                    AssignmentSorters.byUsername()
            );

            assertEquals(2, result.size());
            assertEquals("john_doe", result.get(1).user().username());
            assertEquals("bob_wilson", result.get(0).user().username());
        }
    }

    @Nested
    @DisplayName("Permission and Role Check Tests")
    class PermissionCheckTests {
        private PermanentAssignment permAssign;

        @BeforeEach
        void setUp() {
            permAssign = new PermanentAssignment(user1, adminRole, metadata);
            assignmentManager.add(permAssign);
        }

        @Test
        @DisplayName("userHasRole should return true for assigned role")
        void userHasRoleReturnsTrueForAssignedRole() {
            assertTrue(assignmentManager.userHasRole(user1, adminRole));
            assertFalse(assignmentManager.userHasRole(user1, viewerRole));
        }

        @Test
        @DisplayName("userHasPermission should return true for permissions from roles")
        void userHasPermissionReturnsTrueForRolePermissions() {
            assertTrue(assignmentManager.userHasPermission(user1, "READ", "users"));
            assertTrue(assignmentManager.userHasPermission(user1, "WRITE", "users"));
            assertFalse(assignmentManager.userHasPermission(user1, "DELETE", "reports"));
        }

        @Test
        @DisplayName("getUserPermissions should return all permissions from roles")
        void getUserPermissionsReturnsAllPermissions() {
            Set<Permission> permissions = assignmentManager.getUserPermissions(user1);
            assertEquals(3, permissions.size());
            assertTrue(permissions.contains(readUsers));
            assertTrue(permissions.contains(writeUsers));
            assertTrue(permissions.contains(deleteUsers));
        }

        @Test
        @DisplayName("getUserPermissions for user without roles returns empty Set")
        void getUserPermissionsForUserWithoutRolesReturnsEmptySet() {
            Set<Permission> permissions = assignmentManager.getUserPermissions(user2);
            assertTrue(permissions.isEmpty());
        }

        @Test
        @DisplayName("getUserPermissions aggregates permissions from multiple roles")
        void getUserPermissionsAggregatesFromMultipleRoles() {
            // Add second role to user1
            PermanentAssignment secondAssign = new PermanentAssignment(
                    user1, viewerRole, AssignmentMetadata.now("admin", "Second role")
            );
            assignmentManager.add(secondAssign);

            Set<Permission> permissions = assignmentManager.getUserPermissions(user1);
            assertEquals(3, permissions.size()); // Still 3, viewer role doesn't add new permissions
        }
    }

    @Nested
    @DisplayName("Assignment Management Tests")
    class AssignmentManagementTests {
        private PermanentAssignment permAssign;
        private TemporaryAssignment tempAssign;

        @BeforeEach
        void setUp() {
            permAssign = new PermanentAssignment(user1, adminRole, metadata);
            tempAssign = new TemporaryAssignment(
                    user2, viewerRole, metadata, "2026-12-31 23:59", false
            );
            assignmentManager.add(permAssign);
            assignmentManager.add(tempAssign);
        }

        @Test
        @DisplayName("revokeAssignment for permanent assignment")
        void revokePermanentAssignment() {
            assertTrue(permAssign.isActive());

            assignmentManager.revokeAssignment(permAssign.assignmentId());

            assertFalse(permAssign.isActive());
            assertTrue(permAssign.isRevoked());
            assertFalse(assignmentManager.userHasRole(user1, adminRole));
        }

        @Test
        @DisplayName("revokeAssignment for temporary assignment")
        void revokeTemporaryAssignment() {
            assertTrue(tempAssign.isActive());
            String id = tempAssign.assignmentId();

            assignmentManager.revokeAssignment(id);

            assertFalse(assignmentManager.findById(id).isPresent());
        }

        @Test
        @DisplayName("revokeAssignment for non-existing assignment throws exception")
        void revokeNonExistingAssignmentThrowsException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> assignmentManager.revokeAssignment("non_existing_id")
            );
            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("extendTemporaryAssignment extends temporary assignment")
        void extendTemporaryAssignment() {
            String newDate = "2027-12-31 23:59";

            assignmentManager.extendTemporaryAssignment(tempAssign.assignmentId(), newDate);

            assertEquals(newDate, tempAssign.getExpiresAt());
        }

        @Test
        @DisplayName("extendTemporaryAssignment for permanent assignment throws exception")
        void extendPermanentAssignmentThrowsException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> assignmentManager.extendTemporaryAssignment(
                            permAssign.assignmentId(), "2027-12-31 23:59"
                    )
            );
            assertTrue(exception.getMessage().contains("not temporary"));
        }

        @Test
        @DisplayName("extendTemporaryAssignment for non-existing assignment throws exception")
        void extendNonExistingAssignmentThrowsException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> assignmentManager.extendTemporaryAssignment("non_existing", "2027-12-31 23:59")
            );
            assertTrue(exception.getMessage().contains("not found"));
        }
    }

    @Nested
    @DisplayName("Convenience Methods Tests")
    class ConvenienceMethodsTests {
        private PermanentAssignment permAssign;
        private TemporaryAssignment tempAssign;

        @BeforeEach
        void setUp() {
            permAssign = new PermanentAssignment(user1, adminRole, metadata);
            tempAssign = new TemporaryAssignment(
                    user2, viewerRole, metadata, "2026-12-31 23:59", false
            );
            assignmentManager.add(permAssign);
            assignmentManager.add(tempAssign);
        }

        @Test
        @DisplayName("findByUser should return assignments for specific user")
        void findByUser() {
            List<RoleAssignment> result = assignmentManager.findByUser(user1);
            assertEquals(1, result.size());
            assertEquals(permAssign.assignmentId(), result.get(0).assignmentId());
        }

        @Test
        @DisplayName("findByRole should return assignments for specific role")
        void findByRole() {
            List<RoleAssignment> result = assignmentManager.findByRole(adminRole);
            assertEquals(1, result.size());
            assertEquals(permAssign.assignmentId(), result.get(0).assignmentId());
        }

        @Test
        @DisplayName("getActiveAssignments should return all active assignments")
        void getActiveAssignments() {
            List<RoleAssignment> result = assignmentManager.getActiveAssignments();
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("getExpiredAssignments should return all inactive assignments")
        void getExpiredAssignments() {
            // Initially no expired assignments
            List<RoleAssignment> result = assignmentManager.getExpiredAssignments();
            assertEquals(0, result.size());

            // Revoke one assignment
            assignmentManager.revokeAssignment(permAssign.assignmentId());

            result = assignmentManager.getExpiredAssignments();
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("getPermanentAssignments should return all permanent assignments")
        void getPermanentAssignments() {
            List<RoleAssignment> result = assignmentManager.getPermanentAssignments();
            assertEquals(1, result.size());
            assertTrue(result.get(0) instanceof PermanentAssignment);
        }

        @Test
        @DisplayName("getTemporaryAssignments should return all temporary assignments")
        void getTemporaryAssignments() {
            List<RoleAssignment> result = assignmentManager.getTemporaryAssignments();
            assertEquals(1, result.size());
            assertTrue(result.get(0) instanceof TemporaryAssignment);
        }

        @Test
        @DisplayName("getAssignmentsByAssigner should return assignments by specific assigner")
        void getAssignmentsByAssigner() {
            List<RoleAssignment> result = assignmentManager.getAssignmentsByAssigner("admin");
            assertEquals(2, result.size()); // both assigned by admin
        }

        @Test
        @DisplayName("getAssignmentStatisticsByType should return correct statistics")
        void getAssignmentStatisticsByType() {
            Map<String, Long> stats = assignmentManager.getAssignmentStatisticsByType();

            assertEquals(1, stats.get("PERMANENT"));
            assertEquals(1, stats.get("TEMPORARY"));
            assertEquals(2, stats.get("ACTIVE"));
            assertEquals(0, stats.get("EXPIRED"));
        }
    }

    @Nested
    @DisplayName("Complex Filtering Methods Tests")
    class ComplexFilterTests {
        @BeforeEach
        void setUp() {
            assignmentManager.add(new PermanentAssignment(user1, adminRole, metadata));
            assignmentManager.add(new TemporaryAssignment(
                    user2, viewerRole,
                    AssignmentMetadata.now("manager", "Temp access"),
                    "2026-12-31 23:59", true
            ));
            assignmentManager.add(new PermanentAssignment(
                    user3, editorRole,
                    AssignmentMetadata.now("admin", "Editor role")
            ));
        }

        @Test
        @DisplayName("findComplexAssignments with all parameters")
        void findComplexAssignmentsWithAllParams() {
            List<RoleAssignment> result = assignmentManager.findComplexAssignments(
                    "john_doe", "Administrator", "admin", true
            );

            assertEquals(1, result.size());
            assertEquals("john_doe", result.get(0).user().username());
        }

        @Test
        @DisplayName("findComplexAssignments without assigner")
        void findComplexAssignmentsWithoutAssigner() {
            List<RoleAssignment> result = assignmentManager.findComplexAssignments(
                    "jane_smith", "Viewer", null, true
            );

            assertEquals(1, result.size());
            assertEquals("jane_smith", result.get(0).user().username());
        }

        @Test
        @DisplayName("findComplexAssignments with inactive filter")
        void findComplexAssignmentsWithInactive() {
            List<RoleAssignment> result = assignmentManager.findComplexAssignments(
                    "john_doe", "Administrator", "admin", false
            );

            assertEquals(1, result.size()); // Still active
        }
    }

    @Nested
    @DisplayName("Helper Methods Tests")
    class HelperMethodsTests {
        @Test
        @DisplayName("count should return correct number of assignments")
        void countReturnsCorrectNumber() {
            assertEquals(0, assignmentManager.count());

            assignmentManager.add(new PermanentAssignment(user1, adminRole, metadata));
            assertEquals(1, assignmentManager.count());

            assignmentManager.add(new PermanentAssignment(user2, viewerRole, metadata));
            assertEquals(2, assignmentManager.count());
        }

        @Test
        @DisplayName("findById should return assignment by id")
        void findById() {
            PermanentAssignment assignment = new PermanentAssignment(user1, adminRole, metadata);
            assignmentManager.add(assignment);
            Optional<RoleAssignment> found = assignmentManager.findById(assignment.assignmentId());
            assertTrue(found.isPresent());
            assertEquals(assignment.assignmentId(), found.get().assignmentId());
        }

        @Test
        @DisplayName("clear should remove all assignments")
        void clearRemovesAllAssignments() {
            assignmentManager.add(new PermanentAssignment(user1, adminRole, metadata));
            assignmentManager.add(new PermanentAssignment(user2, viewerRole, metadata));
            assertEquals(2, assignmentManager.count());

            assignmentManager.clear();
            assertEquals(0, assignmentManager.count());
        }
    }
}