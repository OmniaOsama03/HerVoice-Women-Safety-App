package omnia.adu.ac.ae.hervoice;

public abstract class User {
    protected int id;
    protected String firstName;
    protected String lastName;
    protected int age;
    protected String email;
    protected String password;
    protected boolean permission;  // true = Admin = can delete posts, false = User = cannot del posts

    public User(int id, String firstName, String lastName, int age, String email, String password, boolean permission) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.email = email;
        this.password = password;
        this.permission = permission;
    }

    public boolean canDeletePost() {
        return permission;
    }


    // Getters and Setters

    // id
    public int getId() { return id; }

    // First name
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    // Last name
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    // Age
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    // Email
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Password
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    //Permission
    public boolean getPermission() { return permission; }
    public void setPermission(boolean permission) { this.permission = permission; }
}
