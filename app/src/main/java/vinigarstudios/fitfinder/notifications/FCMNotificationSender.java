package vinigarstudios.fitfinder.notifications;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

public class FCMNotificationSender {

    public static void sendFCMLikeNotification(String deviceToken) {
        // Perform network operation asynchronously
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String fcmUrl = "https://fcm.googleapis.com/fcm/send";
                String serverKey = "AAAADvN99lw:APA91bHtV_hBawYTns8z7wVPToqq94gv3Zem4pVqf2_mTHcIivL-OL-5KIXkuuuyBaPFXAg6W3kfWvuiiF4V0rgjjpyUErOq0EU_wxuT8HsETCnT6dAH_XAkKtgwjEijyGQMyKVXHNNN"; // Replace with your FCM server key

                try {
                    // Create URL object
                    URL url = new URL(fcmUrl);

                    // Create connection
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "key=" + serverKey);
                    conn.setDoOutput(true);

                    // Construct JSON payload
                    String notificationBody = "{\"to\":\"" + params[0] + "\",\"notification\":{\"title\":\"You Liked a Post\",\"body\":\"Cool\"}}";

                    // Send the notification
                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(notificationBody.getBytes());
                    outputStream.flush();

                    // Get response code
                    int responseCode = conn.getResponseCode();
                    System.out.println("FCM notification sent, response code: " + responseCode);

                    // Close streams
                    outputStream.close();
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(deviceToken);
    }

    public static void sendFCMDislikeNotification(String deviceToken) {
        // Perform network operation asynchronously
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String fcmUrl = "https://fcm.googleapis.com/fcm/send";
                String serverKey = "AAAADvN99lw:APA91bHtV_hBawYTns8z7wVPToqq94gv3Zem4pVqf2_mTHcIivL-OL-5KIXkuuuyBaPFXAg6W3kfWvuiiF4V0rgjjpyUErOq0EU_wxuT8HsETCnT6dAH_XAkKtgwjEijyGQMyKVXHNNN"; // Replace with your FCM server key

                try {
                    // Create URL object
                    URL url = new URL(fcmUrl);

                    // Create connection
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "key=" + serverKey);
                    conn.setDoOutput(true);

                    // Construct JSON payload
                    String notificationBody = "{\"to\":\"" + params[0] + "\",\"notification\":{\"title\":\"You Disliked a Post\",\"body\":\"Not so Cool\"}}";

                    // Send the notification
                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(notificationBody.getBytes());
                    outputStream.flush();

                    // Get response code
                    int responseCode = conn.getResponseCode();
                    System.out.println("FCM notification sent, response code: " + responseCode);

                    // Close streams
                    outputStream.close();
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(deviceToken);
    }
}
