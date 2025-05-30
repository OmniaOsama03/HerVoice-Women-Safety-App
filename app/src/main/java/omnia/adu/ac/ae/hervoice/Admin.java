package omnia.adu.ac.ae.hervoice;

public class Admin extends User {

    public Admin(int id, String firstName, String lastName, int age, String email, String password, boolean permission) {
        super(id, firstName, lastName, age, email, password, permission);
    }

    // Admin-specific methods ? like delete is in the DatabaseManager class
}

