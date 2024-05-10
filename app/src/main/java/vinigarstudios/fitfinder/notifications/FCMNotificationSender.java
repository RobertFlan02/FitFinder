package vinigarstudios.fitfinder.notifications;

import android.os.AsyncTask;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FCMNotificationSender {

    private static final String TAG = "FCMNotificationSender";
    private static final String SERVER_KEY = "AAAADvN99lw:APA91bHtV_hBawYTns8z7wVPToqq94gv3Zem4pVqf2_mTHcIivL-OL-5KIXkuuuyBaPFXAg6W3kfWvuiiF4V0rgjjpyUErOq0EU_wxuT8HsETCnT6dAH_XAkKtgwjEijyGQMyKVXHNNN";

    public static void sendFCMLikeNotification() {
        FCMTokenManager.getCurrentUserToken(new FCMTokenManager.TokenRetrievedCallback() {
            @Override
            public void onTokenRetrieved(String token) {
                sendFCMNotification(token, "You Liked a Post", "Cool");
            }

            @Override
            public void onTokenRetrievalFailed(Exception exception) {
                Log.e(TAG, "Failed to retrieve FCM token", exception);
            }
        });
    }

    public static void sendFCMDislikeNotification() {
        FCMTokenManager.getCurrentUserToken(new FCMTokenManager.TokenRetrievedCallback() {
            @Override
            public void onTokenRetrieved(String token) {
                sendFCMNotification(token, "You Disliked a Post", "Not so Cool");
            }

            @Override
            public void onTokenRetrievalFailed(Exception exception) {
                Log.e(TAG, "Failed to retrieve FCM token", exception);
            }
        });
    }

    public static void sendFCMPostCreatedNotification() {
        FCMTokenManager.getCurrentUserToken(new FCMTokenManager.TokenRetrievedCallback() {
            @Override
            public void onTokenRetrieved(String token) {
                sendFCMNotification(token, "Post successfully created", "Show them how its done!");
            }

            @Override
            public void onTokenRetrievalFailed(Exception exception) {
                Log.e(TAG, "Failed to retrieve FCM token", exception);
            }
        });
    }

    private static void sendFCMNotification(String deviceToken, String title, String body) {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    String fcmUrl = "https://fcm.googleapis.com/fcm/send";
                    URL url = new URL(fcmUrl);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "key=" + SERVER_KEY);
                    conn.setDoOutput(true);

                    String notificationBody = "{\"to\":\"" + params[0] + "\",\"notification\":{\"title\":\"" + params[1] + "\",\"body\":\"" + params[2] + "\"}}";

                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(notificationBody.getBytes());
                    outputStream.flush();

                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "FCM notification sent, response code: " + responseCode);

                    outputStream.close();
                    conn.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Error sending FCM notification", e);
                }
                return null;
            }
        }.execute(deviceToken, title, body);
    }
}
