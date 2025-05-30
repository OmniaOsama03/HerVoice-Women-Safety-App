package omnia.adu.ac.ae.hervoice;

public class Post {
    private int id;
    private String title;
    private String description;
    private String city;
    private String area;
    private String date;
    private String time;
    private int userId; // Foreign key


    //Constructor
    public Post(int id, String title, String description, String city, String area, String date, String time, int userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.city = city;
        this.area = area;
        this.date = date;
        this.time = time;
        this.userId = userId;
    }


    //Copy Constructor
    public Post(Post other) {
        this.id = other.id;
        this.title = other.title;
        this.description = other.description;
        this.city = other.city;
        this.area = other.area;
        this.date = other.date;
        this.time = other.time;
    }


    // Getters and setters for all fields
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public String getArea() { return area; }

    public void setArea(String area) { this.area = area; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public int getUserId() { return userId; }

    public void setUserId(int userId) { this.userId = userId; }


    //toString

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", city='" + city + '\'' +
                ", area='" + area + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", userId=" + userId +
                '}';
    }
}

