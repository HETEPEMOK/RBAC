public class Main {
    public static void testUser()
    {
        System.out.println("===========User test===========");
        try {
            User user1 = User.create("a", "Fullname", "email@gmail.com");
            System.out.println("[SUCCESS] User 1 (invalid) | " + user1.format());
        }
        catch (Exception e)
        {
            System.out.println("[FAIL] User 1 (invalid) | " + e.getMessage());
        }
        try {
            User user2 = User.create("Username", "", "email@gmail.com");
            System.out.println("[SUCCESS] User 2 (invalid) | " + user2.format());
        }
        catch (Exception e)
        {
            System.out.println("[FAIL] User 2 (invalid) | " + e.getMessage());
        }
        try {
            User user3 = User.create("Username", "Fullname", "email");
            System.out.println("[SUCCESS] User 3 (invalid) | " + user3.format());
        }
        catch (Exception e)
        {
            System.out.println("[FAIL] User 3 (invalid) | " + e.getMessage());
        }
        try {
            User user4 = User.create("Username", "Full name", "email@gmail.com");
            System.out.println("[SUCCESS] User 2 (valid) | " + user4.format());
        }
        catch (Exception e)
        {
            System.out.println("[ERROR] User 4 (valid) | " + e.getMessage());
        }
    }

    public static void main(String args[])
    {
        testUser();

    }
}
