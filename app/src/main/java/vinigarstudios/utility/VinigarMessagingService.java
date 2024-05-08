package vinigarstudios.utility;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import vinigarstudios.fitfinder.MainActivity;
import vinigarstudios.fitfinder.R;

public class VinigarMessagingService extends FirebaseMessagingService
{
    private final static String channelId = "notificationChannel";
    private static String channelName = "com.eazyalgo.fcmpushnotification";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        if (message.getNotification() != null)
        {
            GenerateMessage(this, message.getNotification().getTitle(), message.getNotification().getBody());
        }
    }

    private static void GenerateMessage(Context context, String title, String message)
    {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId).setSmallIcon(R.drawable.baseline_cake_24)
                .setAutoCancel(true)
                .setVibrate(new long[] {1000, 1000, 1000, 1000})
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        builder = builder.setContent(getRemoteView(title, message));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notifcationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notifcationChannel);
        }

        notificationManager.notify(0, builder.build());
    }

    private static RemoteViews getRemoteView(String title, String message)
    {
        RemoteViews remoteView = new RemoteViews(channelName, R.layout.notification);
        remoteView.setTextViewText(R.id.notificationTitle, title);
        remoteView.setTextViewText(R.id.notificationDesc, message);
        remoteView.setImageViewResource(R.id.notificationImage, R.drawable.baseline_cake_24);

        return remoteView;
    }

}
