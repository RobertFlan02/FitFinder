package vinigarstudios.utility;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Random;

public class NotificationHandler {

    public static void sendNotification(String userId, String title, String message) {
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(userId + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(new Random().nextInt(9999)))
                .addData("title", title)
                .addData("body", message)
                .build());
    }
}


