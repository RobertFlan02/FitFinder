package vinigarstudios.fitfinder.models;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

import vinigarstudios.utility.FirebaseHelper;

/**
 * The model class for post (holds data).
 */
public class PostsModel implements IModel {
    private String photoURL;
    private String profileUID;
    private String title;
    private String caption;
    private int likes;
    private Timestamp timestamp; // New field for timestamp

    private UserModel userModel;
    private String userModelJson;
    private String postId;
    private ArrayList<String> userIDsWhoLiked;

    public PostsModel() {
        // Empty constructor needed for Firestore
    }

    public PostsModel(String photoURL, String profileUID, String title, String caption, int likes) {
        this.photoURL = photoURL;
        this.profileUID = profileUID;
        this.title = title;
        this.caption = caption;
        this.postId = profileUID + "_" + "TEMP"; //Temp so we create a model.
        this.likes = likes;
        this.timestamp = Timestamp.now();
        this.userIDsWhoLiked = new ArrayList<>();

        FirebaseHelper.GetCurrentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                userModel = task.getResult().toObject(UserModel.class);
                postId = profileUID + "_" + Integer.toString(getUserModel().getPostsListIds().size());
                userModel.AddToPostsList(PostsModel.this);
                //On complete happens after model is instantiated so we need to replace teh model
                FirebaseHelper.ReplaceModelInDatabase("posts", profileUID + "_" + "TEMP", PostsModel.this);
            }
        });
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
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
    public UserModel getUserModel() {
        return userModel;
    }

    public ArrayList<String> getUserIDsWhoLiked() {
        return userIDsWhoLiked;
    }

    public void setUserIDsWhoLiked(ArrayList<String> userIDsWhoLiked) {
        this.userIDsWhoLiked = userIDsWhoLiked;
    }

    @Override
    public String getDocumentId() {
        return postId;
    }
}