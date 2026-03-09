import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

@DisplayName("RoleManager Tests")
class RoleManagerTest {
    private RoleManager roleManager;
    private UserManager userManager;
    private AssignmentManager assignmentManager;
    private Role adminRole;
    private Role viewerRole;
    private Role editorRole;
    private Permission readUsers;
    private Permission writeUsers;
    private Permission deleteUsers;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        roleManager.setAssignmentManager(assignmentManager);

        readUsers = new Permission("READ", "users", "Can read users");
        writeUsers = new Permission("WRITE", "users", "Can write users");
        deleteUsers = new Permission("DELETE", "users", "Can delete users");

        adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);

        viewerRole = new Role("Viewer", "View only");
        viewerRole.addPermission(readUsers);

        editorRole = new Role("Editor", "Can edit content");
        editorRole.addPermission(readUsers);
        editorRole.addPermission(writeUsers);
    }

    @Nested
    @DisplayName("Role Addition Tests")
    class AddTests {
        @Test
        @DisplayName("Should successfully add a role")
        void addRoleSuccess() {
            roleManager.add(adminRole);
            assertEquals(1, roleManager.count());
            assertTrue(roleManager.exists("Administrator"));
        }

        @Test
        @DisplayName("Should throw exception when adding null role")
        void addNullRoleThrowsException() {
            assertThrows(NullPointerException.class, () -> roleManager.add(null));
        }

        @Test
        @DisplayName("Should throw exception when adding role with existing name")
        void addDuplicateRoleThrowsException() {
            roleManager.add(adminRole);
            Role duplicate = new Role("Administrator", "Duplicate admin");

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> roleManager.add(duplicate)
            );
            assertTrue(exception.getMessage().contains("already exists"));
        }
    }

    @Nested
    @DisplayName("Role Removal Tests")
    class RemoveTests {
        @BeforeEach
        void setUp() {
            roleManager.add(adminRole);
            roleManager.add(viewerRole);
        }

        @Test
        @DisplayName("Should return true when removing existing role without assignments")
        void removeExistingRoleWithoutAssignmentsReturnsTrue() {
            assertTrue(roleManager.remove(adminRole));
            assertEquals(1, roleManager.count());
            assertFalse(roleManager.exists("Administrator"));
        }

        @Test
        @DisplayName("Should return false when removing non-existing role")
        void removeNonExistingRoleReturnsFalse() {
            Role nonExisting = new Role("NonExisting", "Non existing role");
            assertFalse(roleManager.remove(nonExisting));
            assertEquals(2, roleManager.count());
        }

        @Test
        @DisplayName("Should throw exception when removing null role")
        void removeNullThrowsException() {
            assertThrows(NullPointerException.class, () -> roleManager.remove(null));
        }

        @Test
        @DisplayName("Should throw exception when removing role assigned to users")
        void removeAssignedRoleThrowsException() {
            User user = User.create("test_user", "Test User", "test@test.com");
            userManager.add(user);

            AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test");
            PermanentAssignment assignment = new PermanentAssignment(user, viewerRole, metadata);
            assignmentManager.add(assignment);

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> roleManager.remove(viewerRole)
            );
            assertTrue(exception.getMessage().contains("Cannot delete role that is assigned"));
        }
    }

    @Nested
    @DisplayName("Role Search Tests")
    class FindTests {
        @BeforeEach
        void setUp() {
            roleManager.add(adminRole);
            roleManager.add(viewerRole);
            roleManager.add(editorRole);
        }

        @Test
        @DisplayName("Should find role by existing name")
        void findByNameExisting() {
            Optional<Role> found = roleManager.findByName("Administrator");
            assertTrue(found.isPresent());
            assertEquals("Administrator", found.get().getName());
        }

        @Test
        @DisplayName("Should return Optional.empty() for non-existing name")
        void findByNameNonExisting() {
            Optional<Role> found = roleManager.findByName("NonExisting");
            assertTrue(found.isEmpty());
        }

        @Test
        @DisplayName("Should find role by existing id")
        void findByIdExisting() {
            String id = adminRole.getId();
            Optional<Role> found = roleManager.findById(id);
            assertTrue(found.isPresent());
            assertEquals(id, found.get().getId());
        }

        @Test
        @DisplayName("findAll should return all roles")
        void findAllReturnsAllRoles() {
            List<Role> all = roleManager.findAll();
            assertEquals(3, all.size());
        }
    }

    @Nested
    @DisplayName("Role Filtering Tests")
    class FilterTests {
        @BeforeEach
        void setUp() {
            roleManager.add(adminRole);
            roleManager.add(viewerRole);
            roleManager.add(editorRole);
        }

        @Test
        @DisplayName("Filter by exact name")
        void filterByExactName() {
            List<Role> result = roleManager.findByFilter(RoleFilters.byName("Administrator"));
            assertEquals(1, result.size());
            assertEquals("Administrator", result.get(0).getName());
        }

        @Test
        @DisplayName("Filter by name substring")
        void filterByNameContains() {
            List<Role> result = roleManager.findByFilter(RoleFilters.byNameContains("view"));
            assertEquals(1, result.size());
            assertEquals("Viewer", result.get(0).getName());
        }

        @Test
        @DisplayName("Filter by having specific permission")
        void filterByHasPermission() {
            List<Role> result = roleManager.findByFilter(RoleFilters.hasPermission(deleteUsers));
            assertEquals(1, result.size());
            assertEquals("Administrator", result.get(0).getName());
        }

        @Test
        @DisplayName("Filter by having permission by name and resource")
        void filterByHasPermissionByNameAndResource() {
            List<Role> result = roleManager.findByFilter(
                    RoleFilters.hasPermission("WRITE", "users")
            );
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(r ->
                    r.getName().equals("Administrator") || r.getName().equals("Editor")));
        }

        @Test
        @DisplayName("Filter by minimum number of permissions")
        void filterByAtLeastNPermissions() {
            List<Role> result = roleManager.findByFilter(RoleFilters.hasAtLeastNPermissions(3));
            assertEquals(1, result.size());
            assertEquals("Administrator", result.get(0).getName());
        }

        @Test
        @DisplayName("Combined AND filter")
        void combinedAndFilter() {
            RoleFilter filter = RoleFilters.hasPermission(readUsers)
                    .and(RoleFilters.hasAtLeastNPermissions(2));

            List<Role> result = roleManager.findByFilter(filter);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("findAll with filter and sorting by name")
        void findAllWithFilterAndSorter() {
            List<Role> result = roleManager.findAll(
                    RoleFilters.hasPermission(readUsers),
                    RoleSorters.byName()
            );

            assertEquals(3, result.size());
            assertEquals("Administrator", result.get(0).getName());
            assertEquals("Editor", result.get(1).getName());
            assertEquals("Viewer", result.get(2).getName());
        }

        @Test
        @DisplayName("findAll with filter and sorting by permission count")
        void findAllWithFilterAndPermissionCountSorter() {
            List<Role> result = roleManager.findAll(
                    RoleFilters.hasPermission(readUsers),
                    RoleSorters.byPermissionCount()
            );

            assertEquals(3, result.size());
            assertEquals("Viewer", result.get(0).getName());
            assertEquals("Editor", result.get(1).getName());
            assertEquals("Administrator", result.get(2).getName());
        }
    }

    @Nested
    @DisplayName("Permission Management Tests")
    class PermissionManagementTests {
        @BeforeEach
        void setUp() {
            roleManager.add(viewerRole);
        }

        @Test
        @DisplayName("Should add permission to role")
        void addPermissionToRole() {
            assertFalse(viewerRole.hasPermission(writeUsers));

            roleManager.addPermissionToRole("Viewer", writeUsers);

            Optional<Role> updated = roleManager.findByName("Viewer");
            assertTrue(updated.isPresent());
            assertTrue(updated.get().hasPermission(writeUsers));
        }

        @Test
        @DisplayName("Should throw exception when adding permission to non-existing role")
        void addPermissionToNonExistingRoleThrowsException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> roleManager.addPermissionToRole("NonExisting", readUsers)
            );
            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Should remove permission from role")
        void removePermissionFromRole() {
            assertTrue(viewerRole.hasPermission(readUsers));

            roleManager.removePermissionFromRole("Viewer", readUsers);

            Optional<Role> updated = roleManager.findByName("Viewer");
            assertTrue(updated.isPresent());
            assertFalse(updated.get().hasPermission(readUsers));
        }

        @Test
        @DisplayName("Should throw exception when removing permission from non-existing role")
        void removePermissionFromNonExistingRoleThrowsException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> roleManager.removePermissionFromRole("NonExisting", readUsers)
            );
            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("findRolesWithPermission should return roles with specific permission")
        void findRolesWithPermission() {
            roleManager.add(adminRole);
            roleManager.add(editorRole);

            List<Role> result = roleManager.findRolesWithPermission("DELETE", "users");
            assertEquals(1, result.size());
            assertEquals("Administrator", result.get(0).getName());

            List<Role> result2 = roleManager.findRolesWithPermission("READ", "users");
            assertEquals(3, result2.size());
        }
    }

    @Nested
    @DisplayName("Complex Filtering Methods Tests")
    class ComplexFilterTests {
        @BeforeEach
        void setUp() {
            roleManager.add(adminRole);
            roleManager.add(viewerRole);
            roleManager.add(editorRole);
        }

        @Test
        @DisplayName("findRolesByNameAndMinPermissions")
        void findRolesByNameAndMinPermissions() {
            List<Role> result = roleManager.findRolesByNameAndMinPermissions("edit", 1);
            assertEquals(1, result.size());
            assertEquals("Editor", result.get(0).getName());
        }

        @Test
        @DisplayName("findRolesByNameAndMinPermissions with no matches")
        void findRolesByNameAndMinPermissionsNoMatch() {
            List<Role> result = roleManager.findRolesByNameAndMinPermissions("view", 2);
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("findRolesWithEitherPermission")
        void findRolesWithEitherPermission() {
            List<Role> result = roleManager.findRolesWithEitherPermission(deleteUsers, writeUsers);
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(r -> r.getName().equals("Administrator")));
            assertTrue(result.stream().anyMatch(r -> r.getName().equals("Editor")));
        }
    }

    @Nested
    @DisplayName("Helper Methods Tests")
    class HelperMethodsTests {
        @Test
        @DisplayName("count should return correct number of roles")
        void countReturnsCorrectNumber() {
            assertEquals(0, roleManager.count());
            roleManager.add(adminRole);
            assertEquals(1, roleManager.count());
            roleManager.add(viewerRole);
            assertEquals(2, roleManager.count());
        }

        @Test
        @DisplayName("exists should correctly check role existence")
        void existsReturnsCorrectValue() {
            roleManager.add(adminRole);
            assertTrue(roleManager.exists("Administrator"));
            assertFalse(roleManager.exists("NonExisting"));
        }

        @Test
        @DisplayName("clear should remove all roles")
        void clearRemovesAllRoles() {
            roleManager.add(adminRole);
            roleManager.add(viewerRole);
            assertEquals(2, roleManager.count());

            roleManager.clear();
            assertEquals(0, roleManager.count());
            assertFalse(roleManager.exists("Administrator"));
        }
    }
}