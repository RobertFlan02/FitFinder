package vinigarstudios.fitfinder;

public class Photo {
    private String photoURL;
    private String profileUID;

    public Photo() {
        // Empty constructor needed for Firestore
    }

    public Photo(String photoURL, String profileUID) {
        this.photoURL = photoURL;
        this.profileUID = profileUID;
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
}