package omnia.adu.ac.ae.hervoice;

import java.util.ArrayList;

public class Member extends User {
    private String city;
    private ArrayList<Post> posts;

    public Member(int id, String firstName, String lastName, int age, String email, String password, boolean permission, String city) {
        super(id, firstName, lastName, age, email, password, permission);
        this.city = city;
        this.posts = new ArrayList<>();
    }

    //city
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }


    //getPost
    public ArrayList<Post> getPosts() {
        ArrayList<Post> copy = new ArrayList<>();
        for (Post post : posts) {
            copy.add(new Post(post)); // using copy constructor of Post class
        }
        return copy;
    }


    //add & delete posts
    public void addPost(Post post) {
        posts.add(post);
    }

    public void removePost(Post post) {
        posts.remove(post);
    }
}
