package omnia.adu.ac.ae.hervoice;


import android.util.Log;

public class SessionManager {
    private static SessionManager instance;
    private int currentUserId = -1;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        Log.d("SESSION", "Current user ID: " + userId);
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void clearSession() {
        currentUserId = -1;
    }
}

