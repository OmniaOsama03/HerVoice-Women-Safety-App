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
//User table: insert, selectById, updateById
//Post Table: insert, select all, update, delete(only available for admins?), search

public class DatabaseManager extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "HerVoiceDB";
    private static final int DATABASE_VERSION = 7;

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
    //for Activity: EditProfileActivity
    public boolean updateById(int id, String first_name, String last_name, int age, String email, String password, String city) {

        SQLiteDatabase db = this.getWritableDatabase();
            String sqlUpdate;

            if (password == null || password.isEmpty())
            {
                // Don’t update the password (user didn't update field)
                sqlUpdate = "UPDATE User " +
                        "SET first_name = '" + first_name + "', " +
                        "last_name = '" + last_name + "', " +
                        "age = " + age + ", " +
                        "email = '" + email + "', " +
                        "city = '" + city + "' " +
                        "WHERE id = " + id;
            } else {
                // Update password as well
                String hashedPass = hashPassword(password);
                sqlUpdate = "UPDATE User " +
                        "SET first_name = '" + first_name + "', " +
                        "last_name = '" + last_name + "', " +
                        "age = " + age + ", " +
                        "email = '" + email + "', " +
                        "password_hash = '" + hashedPass + "', " +
                        "city = '" + city + "' " +
                        "WHERE id = " + id;
            }

            db.execSQL(sqlUpdate);
            db.close();
            return true;

    }



    //Database operation: Select Member by id
    //for Table: User
    //for Activity: AccountActivity
    public Member selectById(int id) {

        String sqlQuery = "SELECT * FROM User WHERE id = " +id;

        SQLiteDatabase db = this.getWritableDatabase( );
        Cursor cursor = db.rawQuery( sqlQuery, null );

        Member member = null;
        if( cursor.moveToFirst( )  )
            member = new Member(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString( 1 ),
                    cursor.getString( 2 ),
                    Integer.parseInt(cursor.getString(3)),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getInt(6)== 1? true : false,
                    cursor.getString(7)
            );
        return member;

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
    //for Activity: PostActivity
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

        if (cursor.moveToFirst()) {
            do {
                String timeStr = cursor.getString(0); // example: "02:30PM"
                    String hourPart = timeStr.substring(0, timeStr.indexOf(":"));
                    String meridiem = timeStr.substring(timeStr.length() - 2);

                    int hour = Integer.parseInt(hourPart.trim());

                    // Convert to 24-hour format
                    if (meridiem.equalsIgnoreCase("AM")) {
                        if (hour == 12) hour = 0;
                    } else if (meridiem.equalsIgnoreCase("PM")) {
                        if (hour != 12) hour += 12;
                    }

                    // Categorize into buckets
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

        samplePosts.add(new Post(100, "Uncomfortable Stares at the Bus Stop",
                "While waiting for the bus near the mall in the early evening, I noticed a man continuously staring at me. He didn’t say anything or approach, but it made me uneasy enough to walk to another stop further down the road.",
                "Abu Dhabi", "Al Zahiyah", "2025-04-22", "06:45PM", 1));

        samplePosts.add(new Post(101, "Feeling Followed in the Market",
                "While shopping in the local market, I noticed someone who seemed to follow me from aisle to aisle. I changed directions a few times and even paused to see if he would walk away, but he stayed nearby. I quickly finished and left the store.",
                "Sharjah", "Al Nahda", "2025-05-01", "04:10PM", 1));

        samplePosts.add(new Post(102, "Loud Comments While Walking",
                "As I was heading to the metro station around midday, a group across the street made loud comments and laughed. It wasn’t threatening, but it made me uncomfortable enough to avoid that street in the future.",
                "Dubai", "Al Rigga", "2025-04-28", "02:25PM", 1));

        samplePosts.add(new Post(103, "Strange Interaction in the Elevator",
                "I entered an empty elevator, and a man joined right after. Even though there was plenty of space, he stood unusually close and kept glancing at me during the ride. I felt very uneasy until I reached my floor and exited quickly.",
                "Abu Dhabi", "Khalidiya", "2025-05-03", "07:05PM", 1));

        samplePosts.add(new Post(104, "Taxi Ride Made Me Uneasy",
                "I booked a private taxi in the morning, and the driver started asking too many personal questions like where I work and if I live nearby. He wasn’t rude, but the questions felt unnecessary and made me end the ride early.",
                "Dubai", "Jumeirah", "2025-05-06", "10:15AM", 1));

        samplePosts.add(new Post(105, "Repeated Encounters Near My Office",
                "For several days in a row, I noticed the same man standing near the entrance of my office building right around the time I left work. He never said anything or came close, but the pattern felt strange and made me uneasy.",
                "Sharjah", "Al Majaz", "2025-04-29", "05:30PM", 1));

        samplePosts.add(new Post(106, "Comment While Jogging",
                "While jogging at the park early in the morning, someone passing by made an unnecessary comment about my appearance. It wasn’t aggressive, but it threw me off and I decided to end my jog earlier than planned.",
                "Al Ain", "Al Jimi", "2025-05-07", "07:00AM", 1));

        samplePosts.add(new Post(107, "Feeling Watched on the Metro",
                "During my metro ride to work, I noticed a man frequently looking at me. Even when seats became available elsewhere, he moved closer. I felt uncomfortable enough to get off two stops early and wait for the next train.",
                "Dubai", "BurJuman", "2025-05-05", "09:40AM", 1));

        samplePosts.add(new Post(108, "Uncomfortable Stares",
                "While standing at the bus stop in Khalifa City in the late afternoon, I realized a man across the street had been staring for several minutes. He didn’t say anything, but the prolonged stare made me feel unsafe.",
                "Abu Dhabi", "Khalifa City", "2024-11-10", "05:30PM", 1));

        samplePosts.add(new Post(109, "Taxi Driver Comments",
                "During a taxi ride home, the driver started making comments about my looks and asked where I lived. I didn’t feel safe responding, so I stayed quiet and waited until we reached a public area to ask to be dropped off.",
                "Abu Dhabi", "Al Wahda", "2024-12-01", "07:15PM", 1));

        samplePosts.add(new Post(110, "Crowded Mall Incident",
                "While shopping at the mall on a weekend, someone kept brushing past me in the corridor even when it wasn’t crowded. It happened more than once, and it didn’t feel accidental, so I left the store.",
                "Dubai", "Mall of the Emirates", "2025-01-05", "02:20PM", 1));

        samplePosts.add(new Post(111, "Unwanted Attention",
                "As I walked to my car in the parking lot at night, I noticed a man following me and calling out to get my attention. I quickly got into my car, locked the doors, and waited until he walked away.",
                "Sharjah", "Al Majaz", "2025-02-12", "09:45PM", 1));

        samplePosts.add(new Post(112, "Metro Encounter",
                "In a nearly empty metro car during the morning rush, a man stood too close despite having space elsewhere. I moved twice and he followed, until another passenger stepped in and asked if I was okay.",
                "Dubai", "BurJuman Station", "2025-02-25", "08:10AM", 1));

        samplePosts.add(new Post(113, "Unsettling Experience",
                "While walking along the corniche just before sunset, a group of men began whispering and pointing in my direction. They stayed on the sidewalk, but their behavior made me hurry home without finishing my walk.",
                "Abu Dhabi", "Corniche", "2025-03-05", "06:00PM", 1));

        samplePosts.add(new Post(114, "Weird Interaction",
                "At a gas station, a man approached and asked for my number. When I politely declined, he became noticeably irritated and stayed near his car watching me. I waited inside until he left.",
                "Dubai", "Jumeirah", "2025-03-15", "01:45PM", 1));

        samplePosts.add(new Post(115, "Mall Corridor Encounter",
                "I was walking through a quieter corridor in the mall when I noticed a man walking directly behind me for longer than felt normal. I ducked into a store, and he kept walking. It left me feeling uneasy.",
                "Al Ain", "Bawadi Mall", "2025-03-22", "04:30PM", 1));

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
