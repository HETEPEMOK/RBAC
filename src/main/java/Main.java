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
    public static void main(String args[])
    {
        userTest();
        permissionTest();
    }
}
