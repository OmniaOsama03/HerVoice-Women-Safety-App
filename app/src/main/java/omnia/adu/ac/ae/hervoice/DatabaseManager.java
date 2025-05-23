package omnia.adu.ac.ae.hervoice;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


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
                "email TEXT UNIQUE NOT NULL," +
                "password_hash TEXT NOT NULL" +
                ");";

        String createPostTable = "CREATE TABLE Post (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "date TEXT," +
                "time TEXT," +
                "city TEXT," +
                "area TEXT," +
                "description TEXT," +
                "FOREIGN KEY(user_id) REFERENCES User(id)" +
                ");";

        db.execSQL(createUserTable);
        db.execSQL(createPostTable);

        //Add test user manually
        String email = "test@gmail.com";
        String password = "Test12%A";
        String hashed = hashPassword(password);

        String insertTestUser = "INSERT INTO User (email, password_hash) VALUES ('" + email + "', '" + hashed + "');";
        db.execSQL(insertTestUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS Post");
        onCreate(db);
    }

    //Register new user
    public boolean registerUser(String email, String password) {
        //Hash the password
        String hashedPassword = hashPassword(password);

        //Open the database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("email", email);
        cv.put("password_hash", password);

        long insert = db.insert("User",null,cv);
        if (insert ==-1)
            return false;
        else
            return true;


        //Construct the string that holds the SQL statement (OMNIA IS IT OK TO REMOVE THIS LINE?)
        //String sqlInsert = "INSERT INTO User values(null, '" + email + "', " + hashedPassword + ")";

        //Execute the statement (OMNIA CAN I REMOVE THIS LINE TOO?)
        //db.execSQL(sqlInsert);


        //Close the db to avoid keeping it vulnerable
        //db.close();

    }

    //Login method
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
