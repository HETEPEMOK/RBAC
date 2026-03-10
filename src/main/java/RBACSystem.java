import java.util.*;
import java.util.stream.Collectors;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final AuditLog auditLog = new AuditLog();
    private String currentUsr;

    public RBACSystem()
    {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.roleManager.setAssignmentManager(assignmentManager);
        this.currentUsr = "system";
    }
    public AuditLog getAuditLog() {return auditLog;}
    public void initialize()
    {
        if (roleManager.count() > 0 || userManager.count() > 0) {return;}
        Permission readUsers = new Permission("READ", "users", "Can read users");
        Permission writeUsers = new Permission("WRITE", "users", "Can write users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");
        Permission readReports = new Permission("READ", "reports", "Can read reports");
        Permission writeReports = new Permission("WRITE", "reports", "Can write reports");
        Permission readSettings = new Permission("READ", "settings", "Can read settings");
        Permission writeSettings = new Permission("WRITE", "settings", "Can write settings");

        Role adminRole = new Role("Admin", "Full system access");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readReports);
        adminRole.addPermission(writeReports);
        adminRole.addPermission(readSettings);
        adminRole.addPermission(writeSettings);

        Role managerRole = new Role("Manager", "Can manage users and reports");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readReports);
        managerRole.addPermission(writeReports);
        managerRole.addPermission(readSettings);

        Role viewerRole = new Role("Viewer", "View only access");
        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readSettings);
        viewerRole.addPermission(readReports);

        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        try
        {
            User admin = User.create("admin", "System Administrator", "admin@prl.ae");
            userManager.add(admin);

            AssignmentMetadata metadata = AssignmentMetadata.now("system", "Default admin assignment");
            PermanentAssignment assignment = new PermanentAssignment(admin, adminRole, metadata);
            assignmentManager.add(assignment);

            this.currentUsr = admin.username();
        } catch (IllegalArgumentException e)
        {
            System.err.println("Error create admin user: " + e.getMessage());
        }
    }

    public String generateStatistics()
    {
        StringBuilder stats = new StringBuilder();
        stats.append("\n=========== SYSTEM STATISTIC ===========\n");
        stats.append(String.format("Users: %d\n", userManager.count()));
        stats.append(String.format("Roles: %d\n", roleManager.count()));
        stats.append(String.format("Total Assignments: %d\n", assignmentManager.count()));
        stats.append(String.format("\tActive: %d\n", assignmentManager.getActiveAssignments().size()));
        stats.append(String.format("\tExpired/Inactive: %d\n", assignmentManager.getExpiredAssignments().size()));
        stats.append("========================================\n");
        return stats.toString();
    }

    public String getCurrentUsr() {return currentUsr;}
    public UserManager getUserManager() {return userManager;}
    public RoleManager getRoleManager() {return roleManager;}
    public AssignmentManager getAssignmentManager() {return assignmentManager;}
    public void setCurrentUsr(String username)
    {
        if (userManager.exists(username)) this.currentUsr = username;
        else throw new IllegalArgumentException("User not found: " + username);
    }
}
