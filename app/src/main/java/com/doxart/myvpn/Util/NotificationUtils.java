package com.doxart.myvpn.Util;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.doxart.myvpn.R;


public class NotificationUtils {
    private static final String CHANNEL_ID = "DoxyVPN";
    Context context;

    public NotificationUtils(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    public void sendNotification(String title, String content, boolean ongoing) {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("DoxyVPN",
                    "Communication",
                    NotificationManager.IMPORTANCE_DEFAULT);
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        Notification notification = builder.setOngoing(false)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(title)
                .setOngoing(ongoing)
                .setContentText(content)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(PRIORITY_MIN)
                .build();

        manager.notify(1500, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Communication",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager nManager = context.getSystemService(NotificationManager.class);
            if (nManager != null) {
                nManager.createNotificationChannel(channel);
            }
        }
    }

}
