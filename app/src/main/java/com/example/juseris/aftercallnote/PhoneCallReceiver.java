package com.example.juseris.aftercallnote;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.example.juseris.aftercallnote.Activities.ActivityPopupBefore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PhoneCallReceiver extends BroadcastReceiver {

    private SharedPreferences pref;

    @Override
    public void onReceive(Context context, Intent intent) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            String nr = Utils.fixNumber(intent.getExtras().getString("android.intent.extra.PHONE_NUMBER"));
            pref.edit().putString("LastActiveNr", nr).apply();
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            //String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;

            if (TelephonyManager.EXTRA_STATE_IDLE.equals(stateStr)) {

                state = TelephonyManager.CALL_STATE_IDLE;
                pref.edit().putInt("state", state).apply();

            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(stateStr)) {

                state = TelephonyManager.CALL_STATE_OFFHOOK;
                pref.edit().putInt("state", state).apply();

            } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(stateStr)) {
                state = TelephonyManager.CALL_STATE_RINGING;
                pref.edit().putInt("state", state).apply();
                String nr = Utils.fixNumber(intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER));
                pref.edit().putString("LastActiveNr", nr).apply();
            }
            onCallStateChanged(context);
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected void onIncomingCallStarted(Context ctx, Date start) {
    }

    protected void onOutgoingCallStarted(Context ctx, Date start) {
    }

    protected void onIncomingCallEnded(Context ctx, long start, Date end) {
    }

    protected void onOutgoingCallEnded(Context ctx, long start, Date end) {
    }

    protected void onMissedCall(Context ctx, String number, long start) {
    }

    public void onCallStateChanged(Context context) {
        //String nr = pref.getString("LastActiveNr","");
        if (pref.getInt("lastState", 0) == pref.getInt("state", 0)) {
            //No change, debounce extras
            return;
        }

        switch (pref.getInt("state", 0)) {
            case TelephonyManager.CALL_STATE_RINGING:
                pref.edit().putBoolean("isIncoming", true).apply();
                pref.edit().putLong("callStartTime", new Date().getTime()).apply();
                onIncomingCallStarted(context, new Date());
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                boolean show = pref.getBoolean("purpleBox", false);
                if (show) {
                    context.startService(new Intent(context, FlyingButton.class));
                }
                if (pref.getInt("lastState", 0) != TelephonyManager.CALL_STATE_RINGING) {
                    pref.edit().putBoolean("isIncoming", false).apply();
                    pref.edit().putLong("callStartTime", new Date().getTime()).apply();
                    onOutgoingCallStarted(context, new Date());
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (isMyServiceRunning(FlyingButton.class, context)) {
                    context.stopService(new Intent(context, FlyingButton.class));
                }
                if (pref.getInt("lastState", 0) == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context,
                            pref.getString("LastActiveNr", ""),
                            pref.getLong("callStartTime", new Date().getTime()));
                } else {
                    if (pref.getBoolean("isIncoming", true)) {
                        onIncomingCallEnded(context, pref.getLong("callStartTime", new Date().getTime()), new Date());
                    } else {
                        onOutgoingCallEnded(context, pref.getLong("callStartTime", new Date().getTime()), new Date());
                    }
                }
                break;
        }
        pref.edit().putInt("lastState", pref.getInt("state", 0)).apply();
    }

    public String getCallDuration(int callDuration) {
        int seconds = callDuration;
        int minutes = 0;
        while (seconds - 60 >= 0) {
            minutes++;
            seconds -= 60;
        }
        String time = String.valueOf(seconds) + " s";
        if (minutes != 0) {
            time = minutes + " min " + seconds + " s";
        }
        return time;
    }

    public String getCallTime(long callTime) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm", Locale.ENGLISH);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(callTime);
            return formatter.format(calendar.getTime());
        } catch (Exception e) {
            return "";
        }
    }

    public void sendBroadcast(Context ctx) {
        if (ActivityPopupBefore.active) {
            Intent local = new Intent();
            local.setAction("com.hello.action");
            ctx.sendBroadcast(local);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = "";
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }
}

