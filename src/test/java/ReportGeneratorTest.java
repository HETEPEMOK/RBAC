import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@DisplayName("ReportGenerator Tests")
class ReportGeneratorTest {
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private User user1;
    private User user2;
    private Role adminRole;
    private Role viewerRole;
    private Permission readPerm;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        roleManager.setAssignmentManager(assignmentManager);

        readPerm = new Permission("READ", "users", "Can read users");
        Permission writePerm = new Permission("WRITE", "users", "Can write users");

        adminRole = new Role("Admin", "Admin role");
        adminRole.addPermission(readPerm);
        adminRole.addPermission(writePerm);

        viewerRole = new Role("Viewer", "Viewer role");
        viewerRole.addPermission(readPerm);

        roleManager.add(adminRole);
        roleManager.add(viewerRole);

        user1 = User.create("john", "John Doe", "john@test.com");
        user2 = User.create("jane", "Jane Smith", "jane@test.com");
        userManager.add(user1);
        userManager.add(user2);
    }

    @Nested
    @DisplayName("User Report Tests")
    class UserReportTests {
        @Test
        @DisplayName("Should generate user report with assignments")
        void generateUserReportWithAssignments() {
            // Assign roles
            AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
            assignmentManager.add(new PermanentAssignment(user1, adminRole, meta));
            assignmentManager.add(new PermanentAssignment(user2, viewerRole, meta));

            String report = ReportGenerator.generateUserReport(userManager, assignmentManager);

            assertNotNull(report);
            assertTrue(report.contains("USER REPORT"));
            assertTrue(report.contains("john"));
            assertTrue(report.contains("jane"));
            assertTrue(report.contains("Admin"));
            assertTrue(report.contains("Viewer"));
            assertTrue(report.contains("Total users: 2"));
        }

        @Test
        @DisplayName("Should generate user report without assignments")
        void generateUserReportWithoutAssignments() {
            String report = ReportGenerator.generateUserReport(userManager, assignmentManager);

            assertNotNull(report);
            assertTrue(report.contains("USER REPORT"));
            assertTrue(report.contains("john"));
            assertTrue(report.contains("jane"));
            assertTrue(report.contains("Roles: None"));
        }

        @Test
        @DisplayName("Should handle empty user manager")
        void generateUserReportEmpty() {
            UserManager emptyManager = new UserManager();
            String report = ReportGenerator.generateUserReport(emptyManager, assignmentManager);

            assertNotNull(report);
            assertTrue(report.contains("No users found"));
        }
    }

    @Nested
    @DisplayName("Role Report Tests")
    class RoleReportTests {
        @Test
        @DisplayName("Should generate role report with assignments")
        void generateRoleReportWithAssignments() {
            AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
            assignmentManager.add(new PermanentAssignment(user1, adminRole, meta));
            assignmentManager.add(new PermanentAssignment(user2, viewerRole, meta));

            String report = ReportGenerator.generateRoleReport(roleManager, assignmentManager);

            assertNotNull(report);
            assertTrue(report.contains("ROLE REPORT"));
            assertTrue(report.contains("Admin"));
            assertTrue(report.contains("Viewer"));
            assertTrue(report.contains("Permissions: 2"));
            assertTrue(report.contains("Permissions: 1"));
        }

        @Test
        @DisplayName("Should handle empty role manager")
        void generateRoleReportEmpty() {
            RoleManager emptyManager = new RoleManager();
            String report = ReportGenerator.generateRoleReport(emptyManager, assignmentManager);

            assertNotNull(report);
            assertTrue(report.contains("No roles found"));
        }
    }

    @Nested
    @DisplayName("Permission Matrix Tests")
    class PermissionMatrixTests {
        @Test
        @DisplayName("Should generate permission matrix")
        void generatePermissionMatrix() {
            AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
            assignmentManager.add(new PermanentAssignment(user1, adminRole, meta));
            assignmentManager.add(new PermanentAssignment(user2, viewerRole, meta));

            String matrix = ReportGenerator.generatePermissionMatrix(userManager, assignmentManager);

            assertNotNull(matrix);
            assertTrue(matrix.contains("PERMISSION MATRIX"));
            assertTrue(matrix.contains("john"));
            assertTrue(matrix.contains("jane"));
            assertTrue(matrix.contains("READ"));
            assertTrue(matrix.contains("WRITE"));
            assertTrue(matrix.contains("+"));
            assertTrue(matrix.contains("-"));
        }

        @Test
        @DisplayName("Should handle users without permissions")
        void generatePermissionMatrixNoPermissions() {
            String matrix = ReportGenerator.generatePermissionMatrix(userManager, assignmentManager);

            assertNotNull(matrix);
            assertTrue(matrix.contains("No permissions assigned"));
        }
    }

    @Nested
    @DisplayName("Export Tests")
    class ExportTests {
        private static final String TEST_FILE = "test_report.txt";

        @AfterEach
        void cleanup() {
            new File(TEST_FILE).delete();
        }

        @Test
        @DisplayName("Should export report to file")
        void exportToFile() {
            String report = "Test report content";
            ReportGenerator.exportToFile(report, TEST_FILE);

            File file = new File(TEST_FILE);
            assertTrue(file.exists());

            try {
                String content = Files.readString(file.toPath());
                assertEquals(report, content);
            } catch (IOException e) {
                fail("Could not read file");
            }
        }
    }
}