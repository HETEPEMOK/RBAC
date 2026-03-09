import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {
    public static String generateUserReport(UserManager userManager, AssignmentManager assignmentManager)
    {
        StringBuilder report = new StringBuilder();
        report.append("\n========================================\n");
        report.append("\t\t\t\t\tUSER REPORT\n");
        report.append("========================================\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty())
        {
            report.append("No users found.\n");
            return report.toString();
        }

        for (User user: users)
        {
            report.append(String.format("User: %s\n", user.format()));
            report.append(String.format("\tUsername: %s\n", user.username()));
            report.append(String.format("\tFull name: %s\n", user.fullname()));
            report.append(String.format("\tEmail: %s\n", user.email()));

            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            if (assignments.isEmpty())
            {
                report.append("\tRoles: None\n");
            }
            else
            {
                report.append("\tRoles:\n");
                for (RoleAssignment ra: assignments)
                {
                    String status = ra.isActive() ? "ACTIVE" : "INACTIVE";
                    report.append(String.format("\t- %s [%s] (%s)\n",
                            ra.role().getName(), ra.assignmentType(), status));
                    if (ra instanceof TemporaryAssignment temp)
                    {
                        report.append(String.format("\t\tExpires: %s\n", temp.getExpiresAt()));
                    }
                }
            }
            report.append(" ---\n");
        }
        report.append(String.format("\nTotal users: %d\n", users.size()));
        return report.toString();
    }

    public static String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager)
    {
        StringBuilder report = new StringBuilder();
        report.append("\n========================================\n");
        report.append("\t\t\t\t\tROLE REPORT");
        report.append("========================================\n");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty())
        {
            report.append("No roles found.\n");
            return report.toString();
        }

        for (Role role: roles)
        {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            long userCount = assignments.stream()
                    .map(ra -> ra.user().username())
                    .distinct()
                    .count();
            report.append(String.format("Role: %s\n", role.getName()));
            report.append(String.format("\tDescription: %s\n", role.getDescription()));
            report.append(String.format("\tPermissions: %d\n", role.getPermissions().size()));
            report.append(String.format("\tUsers assigned: %d\n", userCount));

            if (!assignments.isEmpty())
            {
                report.append("\tAssigned to:\n");
                assignments.stream()
                        .collect(Collectors.groupingBy(
                                ra -> ra.user().username(),
                                Collectors.mapping(RoleAssignment::assignmentType, Collectors.toSet())
                        ))
                        .forEach((username, types) -> {
                            report.append(String.format("\t\t- %s (%s)\n", username, String.join(", ", types)));
                        });
            }
        }
        report.append(String.format("\nTotal roles: %d\n", roles.size()));
        return report.toString();
    }

    public static String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager)
    {
        StringBuilder report = new StringBuilder();
        report.append("\n========================================\n");
        report.append("\t\t\t\tPERMISSION MATRIX\n");
        report.append("========================================\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty())
        {
            report.append("No users found.\n");
            return report.toString();
        }

        Set<Permission> allPermissions = new HashSet<>();
        for (User user : users) {
            allPermissions.addAll(assignmentManager.getUserPermissions(user));
        }
        if (allPermissions.isEmpty()) {
            report.append("No permissions assigned.\n");
            return report.toString();
        }

        Map<String, List<Permission>> permissionsByResource = allPermissions.stream()
                .collect(Collectors.groupingBy(Permission::resource));

        List<String> resources = new ArrayList<>(permissionsByResource.keySet());
        Collections.sort(resources);

        report.append("Permission Matrix (User × Resource/Permission):\n\n");

        for (String resource : resources) {
            report.append(String.format("\nResource: %s\n", resource));
            report.append("-".repeat(60)).append("\n");

            List<Permission> perms = permissionsByResource.get(resource);
            List<String> permNames = perms.stream()
                    .map(Permission::name)
                    .sorted()
                    .toList();

            report.append(String.format("%-20s", "User"));
            for (String permName : permNames) {
                report.append(String.format(" %-8s", permName));
            }
            report.append("\n");
            report.append("-".repeat(20 + permNames.size() * 9)).append("\n");
            for (User user : users) {
                report.append(String.format("%-20s", user.username()));
                Set<Permission> userPerms = assignmentManager.getUserPermissions(user);

                for (String permName : permNames) {
                    boolean hasPerm = userPerms.stream()
                            .anyMatch(p -> p.resource().equals(resource) && p.name().equals(permName));
                    report.append(String.format(" %-8s", hasPerm ? "+" : "-"));
                }
                report.append("\n");
            }
        }
        report.append("\nLegend: '+' = has permission, '-' = no permission");
        return report.toString();
    }
    public static void exportToFile(String report, String filename)
    {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename)))
        {
            writer.print(report);
            System.out.println("Report saved to: " + filename);
        } catch (java.io.IOException e)
        {
            System.err.println("Error saving report: " + e.getMessage());
        }
    }
}
