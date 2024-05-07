package vinigarstudios.fitfinder;

public class Posts {
    private String photoURL;
    private String profileUID;
    private String title;
    private String caption;

    public Posts() {
        // Empty constructor needed for Firestore
    }

    public Posts(String photoURL, String profileUID, String title, String caption) {
        this.photoURL = photoURL;
        this.profileUID = profileUID;
        this.title= title;
        this.caption= caption;
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
}