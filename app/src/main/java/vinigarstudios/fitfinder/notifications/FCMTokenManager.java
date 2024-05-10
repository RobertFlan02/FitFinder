package vinigarstudios.fitfinder.notifications;

import com.google.firebase.messaging.FirebaseMessaging;

public class FCMTokenManager {

    public interface TokenRetrievedCallback {
        void onTokenRetrieved(String token);
        void onTokenRetrievalFailed(Exception exception);
    }

    // Method used to retrieve current user's FCM token while waiting for completion as process is asynchronous
    public static void getCurrentUserToken(TokenRetrievedCallback callback) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        callback.onTokenRetrieved(token);
                    } else {
                        callback.onTokenRetrievalFailed(task.getException());
                    }
                });
    }
}
