package omnia.adu.ac.ae.hervoice;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//This Class has the following database operations
//There are 2 Tables - User & Post
//User table: insert, update by id
//Post Table: insert, select all, update, delete(only available for admins?), search

public class DatabaseManager extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "HerVoiceDB";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseManager instance;

    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseManager(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE User (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "first_name TEXT," +
                "last_name TEXT," +
                "age INTEGER," +
                "email TEXT UNIQUE NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "permission INTEGER NOT NULL,"+
                "city TEXT" +
                ");";

        String createPostTable = "CREATE TABLE Post (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT,"+
                "description TEXT," +
                "city TEXT," +
                "area TEXT," +
                "date TEXT," +
                "time TEXT," +
                "user_id INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES User(id)" +
                ");";

        db.execSQL(createUserTable);
        db.execSQL(createPostTable);

        //Add test user manually                                                                    //*ADD ADMINS BY HARDCODING HERE
//        String email = "test@gmail.com";
//        String password = "Test12%A";
//        String hashed = hashPassword(password);
//
//        String insertTestUser = "INSERT INTO User (email, password_hash) VALUES ('" + email + "', '" + hashed + "');";
//        db.execSQL(insertTestUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS Post");
        onCreate(db);
    }



    //Database operation: Insert
    //for Table: User
    //for Activity: RegisterActivity
    public boolean registerUser(User user) {                                    //BOTH Members & Admins will be inserted in the User table
        //Hash the password
        String hashedPassword = hashPassword(user.getPassword());

        //Open the database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            String firstName = user.getFirstName();
            String lastName = user.getLastName();
            int age = user.getAge();
            String email = user.getEmail();
            String password = hashedPassword;
            int permission = user.getPermission() ? 1 : 0;                    //In SQLite there are no booleans. Booleans would be stored as int in SQLite with 1 being True and 0 being false
            String city = null;
            if (user instanceof Member) {                                     // If user is a Member, store city as well because members have an extra attribute city which admins don't
                city = ((Member) user).getCity();
            }

            String cityValue = (city != null) ? "'" + city + "'" : "NULL";

            String sqlInsert = "INSERT INTO User values(" +
                    "null, " +
                    "'" + firstName + "', " +
                    "'" + lastName + "', " +
                    age + ", " +
                    "'" + email + "', " +
                    "'" + password + "', " +
                    permission + ", " +
                    cityValue + ");";

            db.execSQL(sqlInsert);
            db.close();
            return true;
        }
        catch (Exception e) {
            db.close();
            return false;

        }


    }


    //Database operation: Select by id
    //for Table: User
    //for Activity: MainActivity
    public boolean loginUser(String email, String password) {
        //Get the password hash
        String hashedPassword = hashPassword(password);

        //Opening the database for reading
        SQLiteDatabase db = this.getReadableDatabase();

        //Constructing the query
        String query = "SELECT id FROM User WHERE email='" + email + "' AND password_hash='" + hashedPassword + "'";
        Cursor cursor = db.rawQuery( query, null);

        boolean success = cursor.moveToFirst();

        if (success) {
            int userId = cursor.getInt(0);
            SessionManager.getInstance().setCurrentUserId(userId);
            Log.d("DB", "User registered successfully: " + email + ", password: " + hashedPassword);
        }

        db.close();
        cursor.close(); //Freeing up the cursor
        return success;
    }


    //Database operation: Update by id
    //for Table: User
    //for Activity: ProfileActivity
    public void updateById( int id, String first_name, String last_name, int age, String email, String password_hash, String city ) {
        SQLiteDatabase db = this.getWritableDatabase();

        String sqlUpdate = "UPDATE User" +
                " set fisrt_name = '" + first_name + "', " +
                "last_name = '" + last_name + "',"+
                "age =" +age+ "," +
                "email = '" + email + "',"+
                "password_hash = '" + password_hash + "',"+
                "city = '" + city +
                " where id = " + id;

        db.execSQL(sqlUpdate);
        db.close( );
    }






    //Database operation: Insert
    //for Table: Post
    //for Activity: IncidentActivity
    public boolean createPost(Post post) {

        //Open the database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            String title = post.getTitle();
            String description = post.getDescription();
            String city = post.getCity();
            String area = post.getArea();
            String date = post.getDate();
            String time = post.getTime();
            int id = post.getUserId();


            String sqlInsert = "INSERT INTO Post values(" +
                    "null, " +
                    "'" + title + "', " +
                    "'" + description + "', " +
                    "'" + city + "', " +
                    "'" + area + "', " +
                    "'" + date + "', " +
                    "'" + time + "', " +
                    id + ");";

            db.execSQL(sqlInsert);
            db.close();
            return true;
        }
        catch (Exception e) {
            db.close();
            return false;

        }


    }


    //Database operation: Search
    //for Table: Post
    //for Activity: HomePageActivity



    //Database operation: Select all
    //for Table: Post
    //for Activity: HomePageActivity



    //Database operation: Update
    //for Table: Post
    //for Activity: ? An activity for members to view their posts and thereby be able to update



    //Database operation: Delete
    //for Table: Post
    //for Activity: Homepage (Only admins can delete posts.) (Members can delete only their post)


    //Another Database operation for Delete posts for Members?




    //Helper to hash password
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash)
                hexString.append(String.format("%02x", b));

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
