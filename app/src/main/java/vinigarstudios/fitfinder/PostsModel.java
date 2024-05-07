package vinigarstudios.fitfinder;

import com.google.firebase.Timestamp;

public class PostsModel {
    private String photoURL;
    private String profileUID;
    private String title;
    private String caption;
    private int likes;
    private Timestamp timestamp; // New field for timestamp

    public PostsModel() {
        // Empty constructor needed for Firestore
    }

    public PostsModel(String photoURL, String profileUID, String title, String caption, int likes) {
        this.photoURL = photoURL;
        this.profileUID = profileUID;
        this.title = title;
        this.caption = caption;
        this.likes = likes;
        this.timestamp = Timestamp.now();
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getProfileUID() {
        return profileUID;
    }

    public void setProfileUID(String profileUID) {
        this.profileUID = profileUID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }


}