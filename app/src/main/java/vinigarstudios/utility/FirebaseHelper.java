package vinigarstudios.utility;

import android.util.Log;

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

import vinigarstudios.fitfinder.models.IModel;

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
     * Uploads Model to the database.
     * @param collectionPath The collection path.
     * @param model The model.
     */
    public static void UploadModelToDatabase(String collectionPath, IModel model)
    {
        try
        {
            FirebaseFirestore.getInstance().collection(collectionPath).add(model);
        }
        catch(Exception e)
        {
            Log.e("FirebaseHelper.UploadModelToDatabase Failed", "Failed Upload because " + e.getMessage());
        }
    }

    /**
     * Uploads Model to the database with a specific ID.
     * @param collectionPath The CollectionPath.
     * @param model The IModel.
     * @param documentId The Id you want the IModel to be located at.
     * @return DocumentReference that got uploaded.
     */
    public static DocumentReference UploadModelToDatabase(String collectionPath, IModel model, String documentId)
    {
        try
        {
            FirebaseFirestore.getInstance().collection(collectionPath).document(documentId).set(model);
        }
        catch(Exception e)
        {
            Log.e("FirebaseHelper.UploadModelToDatabase Failed", "Failed Upload because " + e.getMessage());
        }
        return FirebaseFirestore.getInstance().collection(collectionPath).document(documentId);
    }

    /**
     * Update model in Database.
     * @param collectionPath The collectionPath.
     * @param oldModel The model to be updated.
     * @param newModel oldModel updates into newModel.
     */
    public static void UpdateModelInDatabase(String collectionPath, IModel oldModel, IModel newModel)
    {
        try
        {
            FirebaseFirestore.getInstance().collection(collectionPath).document(oldModel.getDocumentId()).set(newModel);
        }
        catch(Exception e)
        {
            Log.e("FirebaseHelper.UpdateModelInDatabase Failed", "Failed replace because " + e.getMessage());
        }
    }

    /**
     * Replaces a model in database.
     * @param collectionPath The collection path.
     * @param oldModelId The old models id.
     * @param newModel The model replacing it.
     */
    public static void ReplaceModelInDatabase(String collectionPath, String oldModelId, IModel newModel)
    {
        try
        {
            FirebaseFirestore.getInstance().collection(collectionPath).document(oldModelId).delete();
            FirebaseFirestore.getInstance().collection(collectionPath).document(newModel.getDocumentId()).set(newModel);
        }
        catch(Exception e)
        {
            Log.e("FirebaseHelper.ReplaceModelInDatabase Failed", "Failed replace because " + e.getMessage());
        }
    }
}
