public class Main {
    public static void userTest()
    {
        System.out.println("===========User test===========");
        try {
            User user1 = User.create("a", "Fullname", "email@gmail.com");
            System.out.println("[SUCCESS] User 1 (invalid) | " + user1.format());
        }
        catch (Exception e)
        {
            System.out.println("[ERROR] User 1 (invalid) | " + e.getMessage());
        }
        try {
            User user2 = User.create("Username", "", "email@gmail.com");
            System.out.println("[SUCCESS] User 2 (invalid) | " + user2.format());
        }
        catch (Exception e)
        {
            System.out.println("[ERROR] User 2 (invalid) | " + e.getMessage());
        }
        try {
            User user3 = User.create("Username", "Full name", "email");
            System.out.println("[SUCCESS] User 3 (invalid) | " + user3.format());
        }
        catch (Exception e)
        {
            System.out.println("[ERROR] User 3 (invalid) | " + e.getMessage());
        }
        try {
            User user4 = User.create("Username", "Full name", "email@gmail.com");
            System.out.println("[SUCCESS] User 4 (valid) | " + user4.format());
        }
        catch (Exception e)
        {
            System.out.println("[ERROR] User 4 (valid) | " + e.getMessage());
        }
    }
    public static void permissionTest()
    {
        System.out.println("========Permission test========");
        try
        {
            Permission per1 = new Permission("read", "USERS", "Allows reading users");
            System.out.println("[SUCCESS] Permission 1 | " + per1.format());
        } catch (Exception e) {
            System.out.println("[ERROR] Permission 1 | " + e.getMessage());;
        }
        try
        {
            Permission per2 = new Permission("READ EVERYTHING", "users", "Description");
            System.out.println("[SUCCESS] Permission 2 | " + per2.format());
        } catch (Exception e)
        {
            System.out.println("[ERROR] Permission 2 | " + e.getMessage());
        }
        try
        {
            Permission p3 = new Permission("DELETE", "users", "Can delete users");
            System.out.println("matches DEL/use [true]: " + p3.matches("DEL", "use"));
            System.out.println("matches WR/use [false]: " + p3.matches("WR", "use"));
        } catch (Exception e)
        {
            System.out.println("[ERROR] Permission 3 | " + e.getMessage());
        }
    }

    public static void roleTest()
    {
        System.out.println("===========Role test===========");
        Role root = null;
        try{
            root = new Role("root", "CAN DO ANYTHING");
            System.out.println("[SUCCESS] Role 1 (valid) | " + root);
            System.out.println("format: " + root.format());
        }
        catch(Exception e)
        {
            System.out.println("[ERROR] Role 1 | " + e.getMessage());
        }
        try{
            root = new Role("        ", "description");
            System.out.println("[SUCCESS] Role 2 (invalid) | " + root);
        } catch (Exception e)
        {
            System.out.println("[ERROR] Role 2 (invalid) | " + e.getMessage());
        }

        Permission create = new Permission("create","Users", "Can create users");
        root.addPermission(create);

        boolean t1 = root.hasPermission(create);
        System.out.println((t1 ? "[SUCCESS]" : "[ERROR]") + " Role 3 hasPermission(permission) [true]: " + t1);

        boolean t2 = root.hasPermission("create","users");
        System.out.println((t2 ? "[SUCCESS]" : "[ERROR]") + " Role 4 hasPermission(permissionName, resource) [true]: " + t2);

        boolean t3 = root.hasPermission("delete", "user");
        System.out.println((t3 ? "[SUCCESS]" : "[ERROR]") + " Role 5 hasPermission(permissionName, resource) [false]: " + t3);

        root.removePermission(create);

        boolean hasAfterRemove = root.hasPermission(create);
        System.out.println((hasAfterRemove ? "[SUCCESS]" : "[ERROR]") + " Role 6 hasPermission(permission) [false]: " + hasAfterRemove);

        Role other = new Role("Other", "Example description");
        boolean eq = root.equals(other);
        System.out.println((hasAfterRemove ? "[SUCCESS]" : "[ERROR]") + " Role 7 equals(Role2) [false]: " + eq);
    }
    public static void main(String args[])
    {
        userTest();
        permissionTest();
        roleTest();
    }
}
