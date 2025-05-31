package omnia.adu.ac.ae.hervoice;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//This Class has the following database operations
//There are 2 Tables - User & Post
//User table: insert, update by id
//Post Table: insert, select all, update, delete(only available for admins?), search

public class DatabaseManager extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "HerVoiceDB";
    private static final int DATABASE_VERSION = 6;

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

        //Add Sample Posts
        insertSamplePosts(db);

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

            // Query back the newly inserted user using their email
            String query = "SELECT id FROM User WHERE email = '" + email + "'";
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                int userId = cursor.getInt(0);  // Get the 'id' from first column
                SessionManager.getInstance().setCurrentUserId(userId);
                cursor.close();
                db.close();
                return true;
            } else {
                cursor.close();
                db.close();
                return false;
            }
        }
        catch (Exception e) {
            Log.e("DatabaseManager", "registerUser error: " + e.getMessage());
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
                " set first_name = '" + first_name + "', " +
                "last_name = '" + last_name + "',"+
                "age =" +age+ "," +
                "email = '" + email + "',"+
                "password_hash = '" + password_hash + "',"+
                "city = '" + city +
                " where id = " + id;

        db.execSQL(sqlUpdate);
        db.close( );
    }

    // Get user's full name by ID
    public String getUserFullNameById(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT first_name, last_name FROM User WHERE id = " + id;
        Cursor cursor = db.rawQuery(query, null);

        String fullName = "";
        if (cursor.moveToFirst())
        {
            fullName = cursor.getString(0) + " " + cursor.getString(1);
        }

        cursor.close();
        db.close();

        return fullName;
    }

    // Get number of posts by user ID
    public int getPostCountByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM Post WHERE user_id = " + userId;
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst())
        {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();

        return count;
    }

    // Check if email is already in use
    public boolean isEmailTaken(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT id FROM User WHERE email = '" + email + "'";
        Cursor cursor = db.rawQuery(query, null);

        boolean exists = cursor.moveToFirst();

        cursor.close();
        db.close();

        return exists;
    }


    //Database operation: Insert
    //for Table: Post
    //for Activity: IncidentActivity
    public boolean createPost(Post post) {

        //Open the database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            String title = escapeQuotes(post.getTitle());
            String description = escapeQuotes(post.getDescription());
            String city = escapeQuotes(post.getCity());
            String area = escapeQuotes(post.getArea());
            String date = escapeQuotes(post.getDate());
            String time = escapeQuotes(post.getTime());
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
    public ArrayList<Post> getFilteredPosts(String titleKeyword, String city, String area)
    {
        ArrayList<Post> posts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        //Base query
        String query = "SELECT * FROM Post WHERE 1=1"; //Always evaluates to true, so lets us add conditions later. (or you'd need to track first one to add WHERE)
        List<String> args = new ArrayList<>();

        // Apply title filter (if not empty)
        if (titleKeyword != null && !titleKeyword.isEmpty())
        {
            query += " AND title LIKE ?";
            args.add("%" + escapeQuotes(titleKeyword) + "%");
        }

        // Apply city filter (if selected)
        if (city != null && !city.isEmpty()) {
            query+=" AND city = ?";
            args.add(escapeQuotes(city));
        }

        // Apply area filter (if not empty)
        if (area != null && !area.isEmpty()) {
            query += " AND area LIKE ?";
            args.add("%" + escapeQuotes(area) + "%");
        }

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));

        if (cursor.moveToFirst())
        {
            do {
                Post post = new Post(
                        cursor.getInt(0), // id
                        cursor.getString(1), // title
                        cursor.getString(2), // description
                        cursor.getString(3), // city
                        cursor.getString(4), // area
                        cursor.getString(5), // date
                        cursor.getString(6), // time
                        cursor.getInt(7)     // user_id
                );

                posts.add(post);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return posts;
    }



    //Database operation: Select all
    //for Table: Post
    //for Activity: HomePageActivity
    public ArrayList<Post> getAllPosts()
    {
        ArrayList<Post> posts = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Post", null);

        if (cursor.moveToFirst())
        {
            do
            {
                Post post = new Post(
                        cursor.getInt(0), // id
                        cursor.getString(1), // title
                        cursor.getString(2), // description
                        cursor.getString(3), // city
                        cursor.getString(4), // area
                        cursor.getString(5), // date
                        cursor.getString(6), // time
                        cursor.getInt(7)  // user_id
                );

                posts.add(post);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return posts;
    }

    //returns a hashmap of all cities and the number of posts from them
    public Map<String, Integer> getPostCountGroupedByCity()
    {
        Map<String, Integer> postCounts = new HashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT city, COUNT(*) as count FROM Post GROUP BY city";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst())
        {
            do
            {
                String city = cursor.getString(0);
                int count = cursor.getInt(1);

                postCounts.put(city, count);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return postCounts;
    }

    //Returns a hashmap of different timings and the count of posts that occured in them
    public Map<String, Integer> getPostCountByTimeRange() {
        Map<String, Integer> timeBuckets = new HashMap<>();

        timeBuckets.put("Morning", 0);   // 06:00–11:59
        timeBuckets.put("Afternoon", 0); // 12:00–17:59
        timeBuckets.put("Evening", 0);   // 18:00–23:59
        timeBuckets.put("Night", 0);     // 00:00–05:59

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT time FROM Post";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst())
        {
            do
            {
                String timeStr = cursor.getString(0);
                int hour = Integer.parseInt(timeStr.split(":")[0]); //splitting the time based on :  and taking the first part (hour)

                if (hour >= 6 && hour < 12)
                    timeBuckets.put("Morning", timeBuckets.get("Morning") + 1);

                else if (hour >= 12 && hour < 18)
                    timeBuckets.put("Afternoon", timeBuckets.get("Afternoon") + 1);

                else if (hour >= 18 && hour < 24)
                    timeBuckets.put("Evening", timeBuckets.get("Evening") + 1);

                else
                    timeBuckets.put("Night", timeBuckets.get("Night") + 1);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return timeBuckets;
    }

    //Returns all the posts under a certain user id
    public ArrayList<Post> getPostsByUserId(int userId)
    {

        ArrayList<Post> posts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM Post WHERE user_id = " + userId, null);

        if (cursor.moveToFirst())
        {
            do
            {
                Post post = new Post(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getInt(7)
                );

                posts.add(post);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return posts;
    }

    //Deletes a post given the id
    public void deletePost(int postId) {

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM Post WHERE id = " + postId;

        db.execSQL(query);
        db.close();
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

    //Adding Sample Posts
    public void insertSamplePosts(SQLiteDatabase db) {
        List<Post> samplePosts = new ArrayList<>();

        samplePosts.add(new Post(100,   "Uncomfortable Stares at the Bus Stop",
                "While waiting for the bus near the mall, I noticed a man continuously staring at me. He didn't say anything, but it made me uncomfortable enough to walk away and wait at a different stop.",
                "Abu Dhabi", "Al Zahiyah", "2025-04-22", "18:45", 1));

        samplePosts.add(new Post(101, "Feeling Followed in the Market",
                "While shopping in the local market, I had the feeling someone was trailing me aisle to aisle. I changed directions a few times and he still seemed to follow. I left the store quickly.",
                "Sharjah", "Al Nahda", "2025-05-01", "16:10", 1));

        samplePosts.add(new Post(102, "Loud Comments While Walking",
                "As I was walking to the metro station, a group of guys made loud comments from across the street. Nothing threatening, but it made me want to avoid that street again.",
                "Dubai", "Al Rigga", "2025-04-28", "14:25", 1));

        samplePosts.add(new Post(103, "Strange Interaction in the Elevator",
                "I got into an elevator alone, and a man joined right after. He stood unusually close even though the elevator was empty. He kept glancing at me, which made me very anxious until I reached my floor.",
                "Abu Dhabi", "Khalidiya", "2025-05-03", "19:05", 1));

        samplePosts.add(new Post(104, "Taxi Ride Made Me Uneasy",
                "I took a ride with a private taxi driver who asked too many personal questions. He wasn’t rude, but it made me uncomfortable enough to end the ride early.",
                "Dubai", "Jumeirah", "2025-05-06", "10:15", 1));

        samplePosts.add( new Post(105, "Repeated Encounters Near My Office",
                "There’s a man I keep seeing around my office area, often standing near the building's entrance when I leave work. He hasn’t approached me, but it feels too frequent to be coincidence.",
                "Sharjah", "Al Majaz", "2025-04-29", "17:30", 1));

        samplePosts.add(new Post(106, "Comment While Jogging",
                "While jogging in the park, someone passing by made a comment about my outfit. It wasn’t aggressive, but it made me feel self-conscious and I cut my run short.",
                "Al Ain", "Al Jimi", "2025-05-07", "07:00", 1));

        samplePosts.add(new Post(107, "Feeling Watched on the Metro",
                "During my metro ride, I noticed a man watching me constantly. He even moved seats to sit closer when the train wasn’t full. I got off two stops early to avoid further discomfort.",
                "Dubai", "BurJuman", "2025-05-05", "09:40", 1));

        samplePosts.add(new Post(108, "Uncomfortable Stares",
                "While waiting at the bus stop in Khalifa City, I noticed a man staring for an uncomfortably long time. He didn't say anything, but it made me feel unsafe.",
                "Abu Dhabi", "Khalifa City", "2024-11-10", "17:30", 1));

        samplePosts.add(new Post(109, "Taxi Driver Comments",
                "The driver kept asking personal questions and commenting on my appearance. I felt trapped since I was alone in the car.",
                "Abu Dhabi", "Al Wahda", "2024-12-01", "19:15", 1));

        samplePosts.add(new Post(110,"Crowded Mall Incident",
                "While shopping at the mall, someone brushed past me too closely multiple times. It felt intentional and made me really uncomfortable.",
                "Dubai", "Mall of the Emirates", "2025-01-05", "14:20", 1));

        samplePosts.add(new Post(111, "Unwanted Attention",
                "As I walked to my car in the parking lot, a man started following me while calling out things. I rushed into my car and locked it.",
                "Sharjah", "Al Majaz", "2025-02-12", "21:45", 1));

        samplePosts.add(new Post(112, "Metro Encounter",
                "A man stood far too close to me in an almost empty train car. I moved away and he followed until another passenger intervened.",
                "Dubai", "BurJuman Station", "2025-02-25", "08:10", 1));

        samplePosts.add(new Post(113, "Unsettling Experience",
                "I was walking near the corniche when a group of men started whispering and pointing. They didn’t approach, but it made me hurry home.",
                "Abu Dhabi", "Corniche", "2025-03-05", "18:00", 1));

        samplePosts.add(new Post(114, "Weird Interaction",
                "A stranger tried to get my number at a gas station. He got upset when I refused, and I had to wait for him to drive off before I left.",
                "Dubai", "Jumeirah", "2025-03-15", "13:45", 1));

        samplePosts.add(new Post(115, "Mall Corridor Encounter",
                "I was walking alone in a corridor at the mall when a guy started following closely. I turned into a store until he went away.",
                "Al Ain", "Bawadi Mall", "2025-03-22", "16:30", 1));

        // Insert each post into the database
        for (Post post : samplePosts) {
            insertPostDuringCreation(db, post); // Use a new method to insert with existing db
        }
    }

    private void insertPostDuringCreation(SQLiteDatabase db, Post post)
    {
        String title = escapeQuotes(post.getTitle());
        String description = escapeQuotes(post.getDescription());
        String city = escapeQuotes(post.getCity());
        String area = escapeQuotes(post.getArea());
        String date = escapeQuotes(post.getDate());
        String time = escapeQuotes(post.getTime());

        String sqlInsert = "INSERT INTO Post values(" +
                "null, " +
                "'" + title + "', " +
                "'" + description + "', " +
                "'" + city + "', " +
                "'" + area + "', " +
                "'" + date + "', " +
                "'" + time + "', " +
                post.getUserId() + ");";

        db.execSQL(sqlInsert);
    }


    /*Why a separate method was created:
         To avoid getting a writable database while already inside a getWritableDatabase() context (onCreate).
         results in a recursive call which can cause a crash due to "database is locked" or "stack overflow".
     */


    private String escapeQuotes(String input) {
        return input == null ? null : input.replace("'", "''");
    }
}
