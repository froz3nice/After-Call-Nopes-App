package com.example.juseris.aftercallnote;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.juseris.aftercallnote.Activities.ActivityPopupBefore;

/**
 * Created by Juozas on 2017.10.06.
 */

public class StateChecker extends Service {
    private String CHANNEl_ID = "listener_notif";

    public int onStartCommand(Intent intent, int flags, int startId) {

        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
        Notification noti = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M ) {
            noti = new NotificationCompat.Builder(this,CHANNEl_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.is_running))
                    .setSmallIcon(R.drawable.ic_call_icon)
                    .setContentIntent(pendIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .setPriority(Notification.PRIORITY_MIN)
                    .build();
            startForeground(12345, noti);
            IntentFilter filter2 = new IntentFilter();
            filter2.addAction("android.intent.action.PHONE_STATE");
            PhoneCallReceiver mCallReceiver = new PhoneCallReceiver();
            registerReceiver(mCallReceiver, filter2);
        }
        startService(new Intent(this, ServiceNotificationRemover.class));
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

