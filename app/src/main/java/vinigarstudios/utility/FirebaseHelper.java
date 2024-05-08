package vinigarstudios.utility;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import vinigarstudios.fitfinder.UploadActivity;
import vinigarstudios.fitfinder.models.PostsModel;
import vinigarstudios.fitfinder.models.UserModel;

/**
 * Helpers class for FireStore. Contains useful methods usually to get data from the database.
 */
public final class FirebaseHelper {
    /**
     * Gets the current users ID.
     *
     * @return current users ID from FireStore.
     */
    public static String GetCurrentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    /**
     * Gets the current users details.
     *
     * @return current users details from FireStore.
     */
    public static DocumentReference GetCurrentUserDetails() {
        return FirebaseFirestore.getInstance().collection("profiles").document(FirebaseHelper.GetCurrentUserId());
    }

    /**
     * Gets the details of user with {@code userId}.
     *
     * @param userId The user Id of the user you're trying to get the details for.
     * @return User details of userId.
     */
    public static DocumentReference GetOtherUserDetails(String userId) {
        return FirebaseFirestore.getInstance().collection("profiles").document(userId);
    }

    /**
     * Get path to the profileImage of current User.
     *
     * @return profileImageURL of current user.
     */
    public static StorageReference GetCurrentProfilePicStorageRef() {
        return FirebaseStorage.getInstance().getReference().child("profileImageURL")
                .child(FirebaseHelper.GetCurrentUserId());
    }

    /**
     * Get path to the profileImage of other User.
     *
     * @param otherUserId The users Id.
     * @return profileImageUrl of otherUser.
     */
    public static StorageReference GetOtherProfilePicStorageRef(String otherUserId) {
        return FirebaseStorage.getInstance().getReference().child("profileImageURL")
                .child(otherUserId);
    }

    /**
     * Gets the collection "profiles"
     *
     * @return profiles collection
     */
    public static CollectionReference GetAllProfilesCollectionReference() {
        return FirebaseFirestore.getInstance().collection("profiles");
    }


    /**
     * Gets a list of fields from the database e.g. All profile names.
     *
     * @param collectionName The collection name e.g. profiles.
     * @param field          The field from the collection e.g. profileName.
     * @return A list of fields from the database
     */
    //I hate Java generics why can't I do List<T>. 
    public static List<String> GetListFromDatabase(String collectionName, String field) {
        List<String> listOfResults = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(collectionName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                listOfResults.add(document.getString(field));
                            }
                        } else {
                            Log.e("Error in FirebaseHelper.GetUserField", "Couldn't get field of user(maybe field does not exist)");
                        }
                    }
                });
        return listOfResults;
    }

    /**
     * Gets the UserModel of the current user -> UserModel holds the Users data.
     *
     * @return Current Users UserModel.
     */
    public static UserModel GetCurrentUserModel() {
        return FirebaseHelper.GetCurrentUserDetails().get().getResult().toObject(UserModel.class);
    }

    /**
     * Gets the UserModel of a user -> UserModel holds the Users data.
     *
     * @return Users(userId) UserModel.
     */
    public static UserModel GetOtherUserModel(String userId) {
        return FirebaseHelper.GetOtherUserDetails(userId).get().getResult().toObject(UserModel.class);
    }

    /**
     * Uploads Model to the database.
     * @param collectionPath The collection path.
     * @param model The model.
     */
    public static void UploadModelToDatabase(Context context, String collectionPath, PostsModel model)
    {
        try
        {
            Toast.makeText(context, "Post created successfully", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e)
        {
            Log.e("Post Failed", "Failed post because " + e.getMessage());
            Toast.makeText(context, "Error creating Post", Toast.LENGTH_SHORT).show();
        }
        model.setId = FirebaseFirestore.getInstance().collection(collectionPath).add(model).getResult().getId();
    }

    public static void ReplaceModelInDatabase(Context context, String collectionPath, PostsModel oldModel, PostsModel newModel)
    {
        try
        {
            FirebaseHelper.GetCurrentUserDetails().get().getResult().toObject(UserModel.class);
            FirebaseFirestore.getInstance().collection(collectionPath).add(newModel);
            FirebaseFirestore.getInstance().collection(collectionPath).document();

        }
        catch(Exception e)
        {
            Log.e("FirebaseHelper.ReplaceModelInDatabase Failed", "Failed replace because " + e.getMessage());
        }
    }
}
