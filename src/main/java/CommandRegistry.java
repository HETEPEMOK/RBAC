import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandRegistry {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static void printUserTable(List<User> users)
    {
        if (users.isEmpty())
        {
            System.out.println("No users to display.");
            return;
        }
        System.out.println("\n+--------------------------------+----------------------+----------------------------+");
        System.out.println("| Username\t\t\t| Fullname \t\t | Email |");
        System.out.println("+--------------------------------+----------------------+----------------------------+");
        for (User user: users)
        {
            System.out.println(String.format("| %-30s | %-20s | %-26s |", user.username(), user.fullname(), user.email()));
        }
        System.out.println("+--------------------------------+----------------------+----------------------------+");
        System.out.println("Total users: " + users.size());
    }
    private static void printRoleTable(List<Role> roles)
    {
        if (roles.isEmpty())
        {
            System.out.println("No rules to display.");
            return;
        }
        System.out.println("\n+----------------------+----------+-------------+----------------------------------+");
        System.out.println("| Role name\t\t | Perms\t| ID\t\t| Description |");
        System.out.println("+----------------------+----------+-------------+----------------------------------+");
        for (Role role: roles)
        {
            System.out.println(String.format("| %-20s | %-8s | %-11s | %-32s |", role.getName(), role.getPermissions().size(), role.getId(), role.getDescription()));
        }
        System.out.println("+----------------------+----------+-------------+----------------------------------+");
        System.out.println("Total roles: " + roles.size());
    }
    private static void printAssignmentTable(List<RoleAssignment> assignments)
    {
        if (assignments.isEmpty())
        {
            System.out.println("No assignment to display.");
            return;
        }
        System.out.println("\n+--------------------------------+----------------------+------------+--------+---------------------+");
        System.out.println("| User\t\t\t\t | Role\t\t | Typet\t| Status | Assigned At\t|");
        for (RoleAssignment ra: assignments)
        {
            String status = ra.isActive() ? "ACTIVE": "INACTIVE";
            System.out.println(String.format("| $-30s | %-20s | %-10s | %-6s | %-19s |\n",
                    ra.user().username(),
                    ra.role().getName(),
                    ra.assignmentType(),
                    status,
                    ra.metadata().assignedAt()));
        }
        System.out.println("+--------------------------------+----------------------+------------+--------+---------------------+");
        System.out.println("Total assignments: " + assignments.size());
    }

    private static void printPermissionTable(Set<Permission> permissions)
    {
        if (permissions.isEmpty())
        {
            System.out.println("No permissions to display.");
            return;
        }
        System.out.println("\n+----------------------+----------------------+----------------------------------+");
        System.out.println("| Permission\t\t| Resource\t\t| Description |");
        System.out.println("+----------------------+----------------------+----------------------------------+");
        List<Permission> sorted = new ArrayList<>(permissions);
        sorted.sort(Comparator.comparing(Permission::resource).thenComparing(Permission::name));
        for (Permission p : sorted)
        {
            System.out.println(String.format("| %-20s | %-20s | %-32s |\n", p.name(), p.resource(), p.description()));
        }
        System.out.println("+----------------------+----------------------+----------------------------------+");
        System.out.println("Total permissions: " + permissions.size());
    }

    private static String formatAssignmentDetails(RoleAssignment ra)
    {
        String type = ra.assignmentType();
        String status = ra.isActive() ? "ACTIVE" : "INACTIVE";
        String basic = String.format("[%s] %s assigned to %s - %s",
                type, ra.role().getName(), ra.user().username(), status);
        if (ra instanceof TemporaryAssignment temp)
        {
            return basic + String.format(" (expires: %s, auto-renew: %s)",
                    temp.getExpiresAt(), temp.isAutoRenew());
        }
        return basic;
    }

    public static void registerAllCommands(CommandParser parser)
    {
        registerUserCommands(parser);
        registerRoleCommands(parser);
        registerAssignmentCommands(parser);
        registerPermissionCommands(parser);
        registerSystemCommands(parser);
    }

    private static void registerUserCommands(CommandParser parser)
    {
        parser.registerCommand("user-list", "List all users (optional: filter criterial)", ((scanner, system) -> {
            UserManager um = system.getUserManager();
            List<User> users = um.findAll();

            if (users.isEmpty())
            {
                System.out.println("No users found.");
                return;
            }
            printUserTable(users);
        }));
        parser.registerCommand("user-create", "Create a new user", ((scanner, system) -> {
            UserManager um = system.getUserManager();
            try
            {
                String username = ConsoleUtils.promptString(scanner, "Enter username: ", true);
                String fullname = ConsoleUtils.promptString(scanner, "Enter full name: ", true);
                String email = ConsoleUtils.promptString(scanner, "Enter email: ", true);

                User user = User.create(username, fullname, email);
                um.add(user);

                system.getAuditLog().log("CREATE_USER", system.getCurrentUsr(), username,
                        "User created: " + user.format());

                ConsoleUtils.printSuccess("User created successfully: " + user.format());
            } catch (Exception e)
            {
                ConsoleUtils.printError("Error creating user: " + e.getMessage());
            }
        }));

        parser.registerCommand("user-view", "View user details and roles", ((scanner, system) -> {
            UserManager um = system.getUserManager();
            AssignmentManager am = system.getAssignmentManager();

            String username = ConsoleUtils.promptString(scanner, "Enter username: ", true);

            Optional<User> userOpt = um.findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("User not found: " + username);
                return;
            }
            system.getAuditLog().log("VIEW_USER", system.getCurrentUsr(), username, "User viewed: " + username);
            User user = userOpt.get();
            System.out.println("\n=== User Details ===");
            System.out.println(user.format());

            List<RoleAssignment> assignments = am.findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("No role assignments.");
            } else {
                System.out.println("\n=== Role Assignments ===");
                for (RoleAssignment ra : assignments) {
                    System.out.println(" " + formatAssignmentDetails(ra));
                }
            }
            Set<Permission> permissions = am.getUserPermissions(user);
            if (!permissions.isEmpty())
            {
                System.out.println("\n=== Effective Permissions ===");
                permissions.stream().collect(Collectors.groupingBy(Permission::resource))
                        .forEach((resource, perms) -> {
                            System.out.println(" " + resource + ":");
                            perms.forEach(p -> System.out.println("\t - " + p.name()));
                        });
            }
        }));

        parser.registerCommand("user-update", "Update user information", ((scanner, system) -> {
            UserManager um = system.getUserManager();

            String username = ConsoleUtils.promptString(scanner, "Enter username to update: ", true);
            if (!um.exists(username))
            {
                ConsoleUtils.printError("User npt found: " + username);
                return;
            }

            try {
                String newFullName = ConsoleUtils.promptString(scanner, "Enter new full name: ", true);
                String newEmail = ConsoleUtils.promptString(scanner, "Enter new email: ", true);

                um.update(username, newFullName, newEmail);
                ConsoleUtils.printSuccess("User updated successfully.");
                system.getAuditLog().log("UPDATE_USER", system.getCurrentUsr(), username, "Change user: " + username);
            } catch (Exception e)
            {
                ConsoleUtils.printError("Error updating user: " + e.getMessage());
            }
        }));

        parser.registerCommand("user-delete", "Delete a user", ((scanner, system) -> {
            UserManager um = system.getUserManager();
            AssignmentManager am = system.getAssignmentManager();

            String username = ConsoleUtils.promptString(scanner, "Enter username to delete: ", true);
            Optional<User> userOpt = um.findByUsername(username);
            if (userOpt.isEmpty())
            {
                ConsoleUtils.printError("User not found: " + username);
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = am.findByUser(user);

            if (!assignments.isEmpty())
            {
                System.out.println("User has " + assignments.size() + " assignments: ");
                assignments.forEach(a -> System.out.println("  - " + a.role().getName()));

                if (ConsoleUtils.promptYesNo(scanner, "Delete user and all assignments? [Y/N]"))
                {
                    ConsoleUtils.printError("Delete cancelled.");
                    return;
                }

                for (RoleAssignment ra: assignments)
                {
                    am.remove(ra);
                }
            }

            um.remove(user);
            system.getAuditLog().log("DELETE_USER", system.getCurrentUsr(), username, "Delete user: " + username);
            ConsoleUtils.printSuccess("User deleted successfully");
        }));

        parser.registerCommand("user-search", "Search users by filters", ((scanner, system) -> {
            UserManager um = system.getUserManager();

            System.out.println("\n=== Search Filters ===");
            System.out.println("1. Username (contains)");
            System.out.println("2. Email (contains)");
            System.out.println("3. Email domain");
            System.out.println("4. Full name (contains)");
            System.out.println("5. Combined search");
            System.out.print("Choose filter (1-5): ");

            String choice = scanner.nextLine().trim();
            UserFilter filter = null;

            switch (choice)
            {
                case "1":
                    System.out.print("Enter username part: ");
                    String usernamePart = scanner.nextLine().trim();
                    filter = UserFilters.byUsernameContains(usernamePart);
                    break;
                case "2":
                    System.out.print("Enter email part: ");
                    String emailPart = scanner.nextLine().trim();
                    filter = UserFilters.byEmail(emailPart);
                    break;
                case "3":
                    System.out.print("Enter email domain (e.g., @template.com: ");
                    String domain = scanner.nextLine().trim();
                    filter = UserFilters.byEmailDomain(domain);
                    break;
                case "4":
                    System.out.print("Enter full name part: ");
                    String namePart = scanner.nextLine().trim();
                    filter = UserFilters.byFullNameContains(namePart);
                    break;
                case "5":
                    System.out.print("Enter username part: ");
                    String uPart = scanner.nextLine().trim();
                    System.out.print("Enter email domain: ");
                    String eDomain = scanner.nextLine().trim();
                    System.out.print("Enter name part: ");
                    String nPart = scanner.nextLine().trim();

                    filter = UserFilters.byUsernameContains(uPart)
                            .and(UserFilters.byEmailDomain(eDomain))
                            .and(UserFilters.byFullNameContains(nPart));
                    break;
                default:
                    System.err.println("Invalid choice.");
                    return;
            }

            List<User> results = um.findByFilter(filter);
            if (results.isEmpty())
            {
                System.out.println("No users found matching the criteria.");
            } else {
                System.out.println("\nFound " + results.size() + " user(s):");
                printUserTable(results);
            }
        }));
    }

    private static void registerRoleCommands(CommandParser parser)
    {
        parser.registerCommand("role-list", "List all roles", ((scanner, system) -> {
            RoleManager rm = system.getRoleManager();
            List<Role> roles = rm.findAll();
            printRoleTable(roles);
        }));

        parser.registerCommand("role-create", "Create a new role", ((scanner, system) -> {
            RoleManager rm = system.getRoleManager();

            try
            {
                System.out.print("Enter role name: ");
                String name = scanner.nextLine().trim();

                System.out.print("Enter role description: ");
                String description = scanner.nextLine().trim();

                Role role = new Role(name, description);
                rm.add(role);
                System.out.println("Role created successfully with ID: " + role.getId());

                boolean addingPermissions = true;
                while (addingPermissions) {
                    System.out.print("\nAdd permission? [Y/N]: ");
                    String answer = scanner.nextLine().trim().toLowerCase();
                    if (!answer.equals("y")) {
                        addingPermissions = false;
                        continue;
                    }

                    System.out.print("Enter permission name (e.g., READ): ");
                    String permName = scanner.nextLine().trim().toUpperCase();

                    System.out.print("Enter resource (e.g., users): ");
                    String resource = scanner.nextLine().trim().toLowerCase();

                    System.out.print("Enter description: ");
                    String permDesc = scanner.nextLine().trim();

                    try
                    {
                        Permission permission = new Permission(permName, resource, permDesc);
                        rm.addPermissionToRole(name, permission);
                        System.out.println("Permission added.");
                    } catch (IllegalArgumentException e)
                    {
                        System.err.println("Error creating permission: " + e.getMessage());
                    }
                }
                System.out.println("\nRole created successfully:");
                System.out.println(role.format());
            }catch (IllegalArgumentException e)
            {
                System.err.println("Error creating role: " + e.getMessage());
            }
        }));

        parser.registerCommand("role-view", "View role details", ((scanner, system) -> {
            RoleManager rm = system.getRoleManager();

            System.out.print("Enter role name: ");
            String name = scanner.nextLine().trim();

            Optional<Role> roleOpt = rm.findByName(name);
            if (roleOpt.isEmpty())
            {
                System.err.println("Role not found: " + name);
                return;
            }
            System.out.println(roleOpt.get().format());
        }));

        parser.registerCommand("role-update", "Update role name/description", ((scanner, system) -> {
            RoleManager rm = system.getRoleManager();

            System.out.print("Enter role name to update: ");
            String oldName = scanner.nextLine().trim();

            Optional<Role> roleOpt = rm.findByName(oldName);
            if (roleOpt.isEmpty())
            {
                System.err.println("Role not found: " + oldName);
                return;
            }

            Role role = roleOpt.get();
            System.out.println("Current: " + role.getName() + " - " + role.getDescription());

            System.out.print("Enter new description (or press Enter to keep current): ");
            String newDesc = scanner.nextLine().trim();
            if (newDesc.isEmpty())
            {
                newDesc = role.getDescription();
            }
            System.out.println("Note: Role update not fully implemented due to immutability.");
            System.out.println("consider deleting and recreating the role if name/description must change.");
        }));

        parser.registerCommand("role-delete", "Delete a role", ((scanner, system) -> {
            RoleManager rm = system.getRoleManager();
            AssignmentManager am = system.getAssignmentManager();

            System.out.print("Enter role name to delete: ");
            String name = scanner.nextLine().trim();

            Optional<Role> roleOpt = rm.findByName(name);
            if (roleOpt.isEmpty())
            {
                System.err.println("Role not found: " + name);
                return;
            }

            Role role = roleOpt.get();
            List<RoleAssignment> assignments = am.findByRole(role);

            if (!assignments.isEmpty())
            {
                System.out.println("Role is assigned to " + assignments.size() + " user(s):");
                assignments.stream()
                        .map(a -> a.user().username())
                        .distinct()
                        .forEach(username -> System.out.println(" - " + username));
                System.out.print("Delete role anyway? This will remove all assignments. [Y/N]");

                String confirm = scanner.nextLine().trim().toLowerCase();
                if (!confirm.equals("y"))
                {
                    System.out.println("Deletion cancelled.");
                    return;
                }

                for (RoleAssignment ra: assignments)
                {
                    am.remove(ra);
                }
            }

            rm.remove(role);
            System.out.println("Role deleted successfully.");
        }));

        parser.registerCommand("role-add-permission", "Add permission to role", ((scanner, system) -> {
            RoleManager rm = system.getRoleManager();

            System.out.print("Enter role name: ");
            String roleName = scanner.nextLine().trim();

            if (!rm.exists(roleName))
            {
                System.err.println("Role not found: " + roleName);
                return;
            }

            try
            {
                System.out.print("Enter permission name (e.g., READ): ");
                String permName = scanner.nextLine().trim().toUpperCase();

                System.out.print("Enter resource (e.g., users): ");
                String resource = scanner.nextLine().trim().toLowerCase();

                System.out.print("Enter description: ");
                String description = scanner.nextLine().trim();

                Permission permission = new Permission(permName, resource, description);
                rm.addPermissionToRole(roleName, permission);
                System.out.println("Permission added successfully.");
            } catch(IllegalArgumentException e)
            {
                System.err.println("Error adding permission: " + e.getMessage());
            }
        }));

        parser.registerCommand("role-remove-permission", "Remove permission from role", ((scanner, system) -> {
            RoleManager rm = system.getRoleManager();

            System.out.print("Enter role name: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = rm.findByName(roleName);
            if (roleOpt.isEmpty())
            {
                System.err.println("Role not found: " + roleName);
                return;
            }

            Role role = roleOpt.get();
            Set<Permission> permissions = role.getPermissions();

            if (permissions.isEmpty())
            {
                System.err.println("Role has no permissions.");
                return;
            }
            System.out.println("\nPermissions for role '" + roleName + "':");
            List<Permission> permList = new ArrayList<>(permissions);
            for (int i = 0; i < permList.size(); i++)
            {
                Permission p = permList.get(i);
                System.out.println((i + 1) + ". " + p.format());
            }

            System.out.print("Enter number of permissions to remove (0 to cancel): ");
            try
            {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) return;

                if (choice < 1 || choice > permList.size())
                {
                    System.err.println("Invalid choice.");
                    return;
                }

                Permission toRemove = permList.get(choice - 1);
                rm.addPermissionToRole(roleName, toRemove);
                System.out.println("Permission removed successfully");
            } catch (NumberFormatException e)
            {
                System.err.println("Invalid input. Please enter a number.");
            }
        }));

        parser.registerCommand("role-search", "Search roles by filters", ((scanner, system) -> {
            RoleManager rm = system.getRoleManager();

            System.out.println("\n=== Role Search Filters ===");
            System.out.println("1. Name (contains)");
            System.out.println("2. Has specific permission");
            System.out.println("3. Minimum number of permission");
            System.out.print("Choose filter (1-3): ");

            String choice = scanner.nextLine().trim();
            RoleFilter filter = null;

            switch (choice)
            {
                case "1":
                    System.out.print("Enter role name part: ");
                    String namePart = scanner.nextLine().trim();
                    filter = RoleFilters.byNameContains(namePart);
                    break;
                case "2":
                    System.out.print("Enter permission name: ");
                    String permName = scanner.nextLine().trim().toUpperCase();
                    System.out.print("Enter resource: ");
                    String resource = scanner.nextLine().trim().toLowerCase();
                    filter = RoleFilters.hasPermission(permName, resource);
                    break;
                case "3":
                    System.out.print("Enter minimum number of permissions: ");
                    try
                    {
                        int min = Integer.parseInt(scanner.nextLine().trim());
                        filter = RoleFilters.hasAtLeastNPermissions(min);
                    } catch (NumberFormatException e)
                    {
                        System.err.println("Invalid number.");
                        return;
                    }
                    break;
                default:
                    System.err.println("Invalid choice.");
                    return;
            }

            List<Role> results = rm.findByFilter(filter);
            if (results.isEmpty())
            {
                System.out.println("No roles found matching the criteria");
            } else {
                System.out.println("\nFound " + results.size() + " role(s):");
                for (Role role: results)
                {
                    System.out.println("  - " + role.getName() + " (" +
                            role.getPermissions().size() + " permissions)");
                }
            }
        }));
    }

    private static void registerAssignmentCommands(CommandParser parser)
    {
        parser.registerCommand("assign-role", "Assign a role to a user", ((scanner, system) -> {
            UserManager um = system.getUserManager();
            RoleManager rm = system.getRoleManager();
            AssignmentManager am = system.getAssignmentManager();

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = um.findByUsername(username);
            if (userOpt.isEmpty())
            {
                System.err.println("User not found: " + username);
                return;
            }
            User user = userOpt.get();

            List<Role> roles = rm.findAll();
            if (roles.isEmpty())
            {
                System.out.println("No roles available to assign.");
                return;
            }

            System.out.println("\nAvailable roles: ");
            for (int i = 0; i < roles.size(); i++)
            {
                Role role = roles.get(i);
                System.out.println((i + 1) + ". " + role.getName() + " - " + role.getDescription());
            }

            System.out.print("Select role number: ");
            try {
                int roleChoice = Integer.parseInt(scanner.nextLine().trim());
                if (roleChoice < 1 || roleChoice > roles.size())
                {
                    System.err.println("Invalid choice.");
                    return;
                }
                Role selectedRole = roles.get(roleChoice - 1);

                if (am.userHasRole(user, selectedRole))
                {
                    System.out.println("User already has this role active.");
                    return;
                }

                System.out.print("Assignment type (1 - Permanent, 2 - Temporary): ");
                String typeChoice = scanner.nextLine().trim();

                System.out.print("Reason for assignment: ");
                String reason = scanner.nextLine().trim();

                AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUsr(), reason);

                RoleAssignment assignment;

                if (typeChoice.equals("2"))
                {
                    System.out.print("Enter expiration date (yyyy-MM-dd HH:mm): ");
                    String expiresAt = scanner.nextLine().trim();
                    System.out.print("Auto-renew? [Y/N]: ");
                    boolean autoRenew = scanner.nextLine().trim().toLowerCase().equals("y");

                    assignment = new TemporaryAssignment(user, selectedRole, metadata, expiresAt, autoRenew);
                } else {
                    assignment = new PermanentAssignment(user, selectedRole, metadata);
                }
                am.add(assignment);
                System.out.println("Role assigned successfully.");
                System.out.println("Assignment ID: " + assignment.assignmentId());
            } catch (NumberFormatException e)
            {
                System.err.println("Invalid input.");
            } catch (IllegalArgumentException e)
            {
                System.err.println("Error creating assignment: " + e.getMessage());
            }
        }));

        parser.registerCommand("revoke-role", "Revoke a role from a user", ((scanner, system) -> {
            AssignmentManager am = system.getAssignmentManager();
            UserManager um = system.getUserManager();

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = um.findByUsername(username);
            if (userOpt.isEmpty())
            {
                System.err.println("User not found: " + username);
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = am.findByUser(user);

            if (assignments.isEmpty())
            {
                System.out.println("User has no assignments.");
                return;
            }

            System.out.println("\nAssignments for " + username + ":");
            for (int i = 0; i < assignments.size(); i++)
            {
                RoleAssignment ra = assignments.get(i);
                String type = ra.assignmentType();
                String status = ra.isActive() ? "ACTIVE" : "INACTIVE";
                System.out.println((i + 1) + ". [" + type + "] " + ra.role().getName() + " - " + status);
                if (ra instanceof TemporaryAssignment temp)
                {
                    System.out.println("   Expires: " + temp.getExpiresAt());
                }
            }

            System.out.print("Select assignment to revoke (0 to cancel): ");
            try
            {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) return;

                if (choice < 1 || choice > assignments.size())
                {
                    System.err.println("Invalid choice.");
                    return;
                }

                RoleAssignment selected = assignments.get(choice - 1);
                am.revokeAssignment(selected.assignmentId());
                System.out.println("Assignment revoked successfully.");
            } catch (NumberFormatException e)
            {
                System.err.println("Invalid input.");
            }
        }));
        parser.registerCommand("assignment-list", "List all assignments in table format", ((scanner, system) -> {
            AssignmentManager am = system.getAssignmentManager();
            List<RoleAssignment> assignments = am.findAll();
            printAssignmentTable(assignments);
        }));
        parser.registerCommand("assignment-list-user", "List assignments for a user", ((scanner, system) -> {
            AssignmentManager am = system.getAssignmentManager();
            UserManager um = system.getUserManager();

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = um.findByUsername(username);
            if (userOpt.isEmpty())
            {
                System.err.println("User not found: " + username);
                return;
            }

            List<RoleAssignment> assignments = am.findByUser(userOpt.get());
            if (assignments.isEmpty())
            {
                System.out.println("No assignments for user: " + username);
                return;
            }

            System.out.println("\n=== Assignments for " + username + " ===");
            for (RoleAssignment ra: assignments)
            {
                String type = ra.assignmentType();
                String status = ra.isActive() ? "ACTIVE" : "INACTIVE";
                System.out.println(" [" + type + "] " + ra.role().getName() + " - " + status);
                System.out.println("\tAssigned by: " + ra.metadata().assignedBy() + " at " + ra.metadata().assignedAt());
                if (ra.metadata().reason() != null && !ra.metadata().reason().isBlank())
                {
                    System.out.println("\tReason: " + ra.metadata().reason());
                }
                if (ra instanceof TemporaryAssignment temp)
                {
                    System.out.println("\tExpires: " + temp.getExpiresAt() + (temp.isAutoRenew() ? " (auto-renew)": ""));
                }
                System.out.println(" ---");
            }
        }));

        parser.registerCommand("assignment-list-role", "List users with a specific role", ((scanner, system) -> {
            AssignmentManager am = system.getAssignmentManager();
            RoleManager rm = system.getRoleManager();

            System.out.print("Enter role name: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = rm.findByName(roleName);
            if (roleOpt.isEmpty())
            {
                System.err.println("Role not found: " + roleName);
                return;
            }

            List<RoleAssignment> assignments = am.findByRole(roleOpt.get());
            if (assignments.isEmpty())
            {
                System.out.println("No users have role: " + roleName);
                return;
            }

            System.out.println("\n=== Users with role '" + roleName + "' ===");
            assignments.stream()
                    .collect(Collectors.groupingBy(
                            ra -> ra.user().username(),
                            Collectors.toList()
                    ))
                    .forEach((username, userAssignments) -> {
                        System.out.println("  - " + username + ":");
                        for (RoleAssignment ra: userAssignments)
                        {
                            System.out.println("\t" + ra.assignmentType() + " (" + (ra.isActive() ? "ACTIVE" : "INACTIVE") + ")");
                        }
                    });
        }));

        parser.registerCommand("assignment-active", "List all active assignments", ((scanner, system) -> {
            AssignmentManager am = system.getAssignmentManager();
            List<RoleAssignment> active = am.getActiveAssignments();
            printAssignmentTable(active);
        }));

        parser.registerCommand("assignment-expired", "List expired assignments", ((scanner, system) -> {
            AssignmentManager am = system.getAssignmentManager();
            List<RoleAssignment> expired = am.getExpiredAssignments();
            printAssignmentTable(expired);
        }));

        parser.registerCommand("assignment-extend", "Extend a temporary assignment", ((scanner, system) -> {
            AssignmentManager am = system.getAssignmentManager();

            System.out.print("Enter assignment ID: ");
            String assignmentId = scanner.nextLine().trim();

            Optional<RoleAssignment> assignmentOpt = am.findById(assignmentId);
            if (assignmentOpt.isEmpty())
            {
                System.err.println("Assignment not found.");
                return;
            }

            if (!(assignmentOpt.get() instanceof TemporaryAssignment))
            {
                System.err.println("Only temporary assignments can be extended.");
                return;
            }

            System.out.print("Enter new expiration date (yyyy-MM-dd HH:mm): ");
            String newDate = scanner.nextLine().trim();

            try
            {
                am.extendTemporaryAssignment(assignmentId, newDate);
                System.out.println("Assignment extended successfully.");
            }catch (IllegalArgumentException e)
            {
                System.err.println("Error: " + e.getMessage());
            }
        }));

        parser.registerCommand("assignment-search", "Search assignments by filters", ((scanner, system) -> {
            AssignmentManager am = system.getAssignmentManager();

            System.out.println("\n=== Assignment Search Filters ===");
            System.out.println("1. By user");
            System.out.println("2. By role");
            System.out.println("3. By type (PERMANENT/TEMPORARY)");
            System.out.println("4. By status (active/inactive)");
            System.out.println("5. Assigned after date");
            System.out.println("6. Expiring before date");
            System.out.print("Choose filter (1-6)");

            String choice = scanner.nextLine().trim();
            AssignmentFilter filter = null;

            switch (choice)
            {
                case "1":
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine().trim();
                    filter = AssignmentFilters.byUsername(username);
                    break;
                case "2":
                    System.out.print("Enter role name: ");
                    String rolename = scanner.nextLine().trim();
                    filter = AssignmentFilters.byRoleName(rolename);
                    break;
                case "3":
                    System.out.print("Enter type (PERMANENT/TEMPORARY): ");
                    String type = scanner.nextLine().trim().toUpperCase();
                    filter = AssignmentFilters.byType(type);
                    break;
                case "4":
                    System.out.print("Enter status (active/inactive): ");
                    String status = scanner.nextLine().trim().toLowerCase();
                    filter = status.equals("active") ? AssignmentFilters.activeOnly() : AssignmentFilters.inactiveOnly();
                    break;
                case "5":
                    System.out.print("Enter date (yyyy-MM-dddd HH:mm): ");
                    String date = scanner.nextLine().trim();
                    filter = AssignmentFilters.assignmentAfter(date);
                    break;
                case "6":
                    System.out.print("Enter date (yyyy-MM-dddd HH:mm): ");
                    String expDate = scanner.nextLine().trim();
                    filter = AssignmentFilters.expiringBefore(expDate);
                    break;
                default:
                    System.err.println("Invalid choice.");
                    break;
            }

            List<RoleAssignment> result = am.findByFilter(filter);
            if (result.isEmpty())
            {
                System.out.println("No assignments found matching the criteria.");
            } else {
                System.out.println("\nFound " + result.size() + " assignment(s):");
                printAssignmentTable(result);
            }
        }));
    }

    private static void registerPermissionCommands(CommandParser parser)
    {
        parser.registerCommand("permissions-user", "List all permissions for a user", ((scanner, system) -> {
            UserManager um = system.getUserManager();
            AssignmentManager am = system.getAssignmentManager();

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = um.findByUsername(username);
            if (userOpt.isEmpty())
            {
                System.err.println("User not found: " + username);
                return;
            }

            User user = userOpt.get();
            Set<Permission> permissions = am.getUserPermissions(user);

            if (permissions.isEmpty())
            {
                System.out.println("User has no permissions.");
                return;
            }

            System.out.println("\n=== Permissions for " + username + " ===");

            Map<String, List<Permission>> byResource = permissions.stream()
                    .collect(Collectors.groupingBy(Permission::resource));

            for (Map.Entry<String, List<Permission>> entry: byResource.entrySet())
            {
                System.out.println("\n" + entry.getKey() + ":");
                for (Permission p : entry.getValue())
                {
                    System.out.println("  - " + p.name() + ": " + p.description());
                }
            }
            System.out.println("\nTotal permissions: " + permissions.size());
        }));

        parser.registerCommand("permissions-check", "Check if user has secific permission", ((scanner, system) -> {
            UserManager um = system.getUserManager();
            AssignmentManager am = system.getAssignmentManager();

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = um.findByUsername(username);
            if (userOpt.isEmpty())
            {
                System.err.println("USer not found: " + username);
                return;
            }

            User user = userOpt.get();

            System.out.print("Enter permission name (e.g., READ): ");
            String permName = scanner.nextLine().trim().toUpperCase();

            System.out.print("Enter resource (e.g., users): ");
            String resource = scanner.nextLine().trim().toLowerCase();

            boolean hasPermission = am.userHasPermission(user, permName, resource);

            if (hasPermission)
            {
                System.out.println("[+] User HAS permission " + permName + " on " + resource);

                List<RoleAssignment> assignments = am.findByUser(user);
                for (RoleAssignment ra: assignments)
                {
                    if (ra.isActive() && ra.role().hasPermission(permName, resource))
                    {
                        System.out.println("\t- Granted by role: " + ra.role().getName());
                    }
                }
            } else {
                System.out.println("[-] User does NOT HAVE permission " + permName + " on " + resource);
            }
        }));
    }

    private static void registerSystemCommands(CommandParser parser)
    {
        parser.registerCommand("help", "Show this help message", ((scanner, system) -> {
            parser.printHelp();
        }));

        parser.registerCommand("stats", "Show system statistics", ((scanner, system) -> {
            System.out.println(system.generateStatistics());
        }));

        parser.registerCommand("clear", "Clear the screen", ((scanner, system) -> {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }));

        parser.registerCommand("exit", "Exit the program", ((scanner, system) -> {
            System.out.print("Are you sure want to exit? [Y/N]: ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("y"))
            {
                System.out.println("The journey is end");
                System.exit(0);
            } else {
                System.out.println("Exit cancelled");
            }
        }));

        parser.registerCommand("audit-log", "View audit log", ((scanner, system) -> {
            system.getAuditLog().printLog();
        }));

        parser.registerCommand("audit-log-save", "Save audit log to file", ((scanner, system) -> {
            String filename = ConsoleUtils.promptString(scanner, "Enter filename: ", true);
            system.getAuditLog().saveToFile(filename);
        }));

        parser.registerCommand("report-users", "Generate user report", ((scanner, system) -> {
            String report = ReportGenerator.generateUserReport(
                    system.getUserManager(),
                    system.getAssignmentManager()
            );
            System.out.println(report);
            if (ConsoleUtils.promptYesNo(scanner, "Save as file?"))
            {
                String filename = ConsoleUtils.promptString(scanner, "Enter filename: ", true);
                ReportGenerator.exportToFile(report, filename);
            }
        }));

        parser.registerCommand("report-roles", "Generate role report", (scanner, system) -> {
            String report = ReportGenerator.generateRoleReport(
                    system.getRoleManager(),
                    system.getAssignmentManager()
            );
            System.out.println(report);

            if (ConsoleUtils.promptYesNo(scanner, "Save to file?")) {
                String filename = ConsoleUtils.promptString(scanner, "Enter filename: ", true);
                ReportGenerator.exportToFile(report, filename);
            }
        });

        parser.registerCommand("report-matrix", "Generate permission matrix", (scanner, system) -> {
            String report = ReportGenerator.generatePermissionMatrix(
                    system.getUserManager(),
                    system.getAssignmentManager()
            );
            System.out.println(report);

            if (ConsoleUtils.promptYesNo(scanner, "Save to file?")) {
                String filename = ConsoleUtils.promptString(scanner, "Enter filename: ", true);
                ReportGenerator.exportToFile(report, filename);
            }
        });
    }
}
