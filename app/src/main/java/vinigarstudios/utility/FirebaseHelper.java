package vinigarstudios.utility;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import vinigarstudios.fitfinder.R;
import vinigarstudios.fitfinder.activites.MainActivity;
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

//    /**
//     * Gets the ModelTask. Used in any GetModel method.
//     * @param collection The collection
//     * @param id The Id
//     * @return The model task.
//     */
//    private static Task<DocumentSnapshot> GetModelTask(String collection, String id)
//    {
//        return FirebaseFirestore.getInstance().collection(collection).document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//            }
//        });
//    }
//
//    /**
//     * Get the userModel of Id.
//     * @param id The id of the userModel.
//     * @return userModel of Id.
//     */
//    public static UserModel GetUserModelFromId(String id)
//    {
//        return GetModelTask("profiles", id).getResult().toObject(UserModel.class);
//    }
//
//    /**
//     * Get the postModel of Id.
//     * @param id The id of the postModel.
//     * @return postModel of Id.
//     */
//    public static PostsModel GetPostsModelFromId(String id)
//    {
//        return GetModelTask("posts", id).getResult().toObject(PostsModel.class);
//    }
//
//    /**
//     * Get the friendModel of Id.
//     * @param id The id of the friendModel.
//     * @return friendModel of Id.
//     */
//    public static FriendRequestModel GetFriendReqModelFromId(String id)
//    {
//        return GetModelTask("posts", id).getResult().toObject(FriendRequestModel.class);
//    }


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

    /**
     * Removes a model in database.
     * @param collectionPath The collection path.
     * @param model The model.
     */
    public static void RemoveModelInDatabase(String collectionPath, IModel model)
    {
        try
        {
            FirebaseFirestore.getInstance().collection(collectionPath).document(model.getDocumentId()).delete();
        }
        catch(Exception e)
        {
            Log.e("FirebaseHelper.ReplaceModelInDatabase Failed", "Failed replace because " + e.getMessage());
        }
    }

    /**
     * Removes a model in database.
     * @param collectionPath The collection path.
     * @param modelId The models id.
     */
    public static void RemoveModelInDatabase(String collectionPath, String modelId)
    {
        try
        {
            FirebaseFirestore.getInstance().collection(collectionPath).document(modelId).delete();
        }
        catch(Exception e)
        {
            Log.e("FirebaseHelper.ReplaceModelInDatabase Failed", "Failed replace because " + e.getMessage());
        }
    }

    public static class MyFirebaseMessagingService extends FirebaseMessagingService {

        private static final String TAG = "MyFirebaseMsgService";


        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            if (remoteMessage.getNotification()!=null){
                // Show the notification
                String notificationBody = remoteMessage.getNotification().getBody();
                String notificationTitle = remoteMessage.getNotification().getTitle();

                sendNotification (notificationTitle, notificationBody);

            }
        }


        private void sendNotification(String title, String body) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_IMMUTABLE);

            String channelId = "fcm_default_channel";
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(body)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }


    }
}
