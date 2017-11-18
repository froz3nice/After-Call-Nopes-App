package com.example.juseris.aftercallnote;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.juseris.aftercallnote.Activities.ActivityPopupBefore;

/**
 * Created by Juozas on 2017.10.07.
 */

public class ServiceNotificationRemover extends Service {
    private String CHANNEl_ID = "listener_notif";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
        Notification noti = new NotificationCompat.Builder(this,CHANNEl_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.is_running))
                .setSmallIcon(R.drawable.ic_call_icon)
                .setContentIntent(pendIntent)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setPriority(Notification.PRIORITY_MIN)
                .build();

        startForeground(12345, noti);
        stopForeground(true);
        stopSelf();
    }
}
