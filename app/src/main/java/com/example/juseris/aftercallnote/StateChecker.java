package com.example.juseris.aftercallnote;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class StateChecker extends Service implements PhoneCallReceiver.Listener {

    public int onStartCommand(Intent intent, int flags, int startId) {

        //The intent to launch when the user clicks the expanded notification
        Intent intent1 = new Intent(this, ActivityPopupBefore.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent1, 0);
        Notification noti = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            noti = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.is_running))
                    .setSmallIcon(R.drawable.ic_call_icon)
                    .setContentIntent(pendIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .setPriority(Notification.PRIORITY_MIN)
                    .build();
            startForeground(12345, noti);
        }

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.PHONE_STATE");
        filter2.addAction("android.intent.action.PROCESS_OUTGOING_CALLS");

        PhoneCallReceiver mCallReceiver = new PhoneCallReceiver();
        registerReceiver(mCallReceiver, filter2);
        mCallReceiver.registerListener(this);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCallStateChanged(Context context, int state, String nr) {
        ReceiverHelperService helper = new ReceiverHelperService();
        helper.onCallStateChanged(context,state,nr);
    }
}

