package vinigarstudios.fitfinder.notifications;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

public class FCMTokenManager {

    private static final String TAG = "FCMTokenManager";

    public interface TokenRetrievedCallback {
        void onTokenRetrieved(String token);
        void onTokenRetrievalFailed(Exception exception);
    }

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
