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

public final class FirebaseHelper
{
    /**
     * Gets the current users ID.
     * @return current users ID from FireStore.
     */
    public static String GetCurrentUserId()
    {
        return FirebaseAuth.getInstance().getUid();
    }

    /**
     * Gets the current users details.
     * @return current users details from FireStore.
     */
    public static DocumentReference GetCurrentUserDetails()
    {
        return FirebaseFirestore.getInstance().collection("profiles").document(FirebaseHelper.GetCurrentUserId());
    }

    public static StorageReference GetCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("profileImageURL")
                .child(FirebaseHelper.GetCurrentUserId());
    }

    public static StorageReference  GetOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child("profileImageURL")
                .child(otherUserId);
    }

    public static CollectionReference GetAllProfilesCollectionReference()
    {
        return FirebaseFirestore.getInstance().collection("profiles");
    }


    /**
     * Gets a list of fields from the database e.g. All profile names.
     * @param collectionName The collection name e.g. profiles.
     * @param field The field from the collection e.g. profileName.
     * @return A list of fields from the database
     */
    //I hate Java generics why can't I do List<T>. 
    public static List<String> GetListFromDatabase(String collectionName, String field)
    {
        List<String> listOfResults = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(collectionName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                listOfResults.add(document.getString(field));
                            }
                        } else
                        {
                            Log.e("Error in FirebaseHelper.GetUserField", "Couldn't get field of user(maybe field does not exist)");
                        }
                    }
                });
        return listOfResults;
    }
}
