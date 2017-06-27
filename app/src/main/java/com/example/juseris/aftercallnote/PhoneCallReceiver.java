package com.example.juseris.aftercallnote;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Activities.ActivityPopupAfter;
import com.example.juseris.aftercallnote.Activities.ActivityPopupBefore;
import com.example.juseris.aftercallnote.Models.ClassSettings;
import com.example.juseris.aftercallnote.Models.ContactsEntity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
            String nr = fixNumber(intent.getExtras().getString("android.intent.extra.PHONE_NUMBER"));
            pref.edit().putString("LastActiveNr", nr).apply();
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;

            if (TelephonyManager.EXTRA_STATE_IDLE.equals(stateStr)) {

                state = TelephonyManager.CALL_STATE_IDLE;
                pref.edit().putInt("state",state).apply();

            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(stateStr)) {

                state = TelephonyManager.CALL_STATE_OFFHOOK;
                pref.edit().putInt("state",state).apply();

            } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(stateStr)) {

                state = TelephonyManager.CALL_STATE_RINGING;
                pref.edit().putInt("state",state).apply();
                String nr = fixNumber(intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER));
                pref.edit().putString("LastActiveNr", nr).apply();

            }
            onCallStateChanged(context, state, pref.getString("LastActiveNr","911"));
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected void onIncomingCallStarted(Context ctx, String number, Date start){}
    protected void onOutgoingCallStarted(Context ctx, String number, Date start){}
    protected void onIncomingCallEnded(Context ctx, String number,long start, Date end){}
    protected void onOutgoingCallEnded(Context ctx, String number, long start, Date end){}
    protected void onMissedCall(Context ctx, String number, long start){}

    public void onCallStateChanged(Context context, int state, String number) {
        String nr = PreferenceManager.getDefaultSharedPreferences(context).getString("LastActiveNr","");
        if(pref.getInt("lastState",0) == pref.getInt("state",0)){
            //No change, debounce extras
            return;
        }

        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                pref.edit().putBoolean("isIncoming",true).apply();
                pref.edit().putLong("callStartTime",new Date().getTime()).apply();
                onIncomingCallStarted(context, nr,new Date());
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                boolean show = pref.getBoolean("purpleBox", false);
                if(show){
                    context.startService(new Intent(context, FlyingButton.class));
                }
                if( pref.getInt("lastState",0) != TelephonyManager.CALL_STATE_RINGING){
                    pref.edit().putBoolean("isIncoming",false).apply();
                    pref.edit().putLong("callStartTime",new Date().getTime()).apply();
                    onOutgoingCallStarted(context,nr , new Date());
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if(isMyServiceRunning(FlyingButton.class,context)) {
                    context.stopService(new Intent(context, FlyingButton.class));
                }
                if(pref.getInt("lastState",0) == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onMissedCall(context, nr, pref.getLong("callStartTime",new Date().getTime()));
                }
                else{
                    if(pref.getBoolean("isIncoming",true)){
                        onIncomingCallEnded(context, nr, pref.getLong("callStartTime",new Date().getTime()), new Date());
                    }
                    else{
                        onOutgoingCallEnded(context, nr, pref.getLong("callStartTime",new Date().getTime()), new Date());
                    }
                }
                break;
        }
        pref.edit().putInt("lastState",state).apply();
    }
    //-------------------------------------------------------------------------

    public String getCallDuration(int callDuration) {
        int seconds = callDuration;
        int minutes = 0;
        while (seconds - 60 >= 0) {
            minutes++;
            seconds -= 60;
        }
        String time = String.valueOf(seconds)+" s";
        if(minutes != 0){
            time = minutes+" min "+seconds+" s";
        }
        return time;
    }

    public String getCallTime(long callTime) {
        String tm = "hey";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(callTime);
            tm = formatter.format(calendar.getTime());
        }catch (Exception e) {
            tm = "";
        }
        return tm;
    }

    public void sendBroadcast(Context ctx){
        if (ActivityPopupBefore.active) {
            Intent local = new Intent();
            local.setAction("com.hello.action");
            ctx.sendBroadcast(local);
        }
    }


    public void setNumberToSharedPref(Context context) {
        String numbers = pref.getString("NUMBERS", "");
        if (numbers.equals("")) {
            numbers += pref.getString("LastActiveNr", "");
        } else {
            numbers += ";" + pref.getString("LastActiveNr", "");
        }
        pref.edit().putString("NUMBERS", numbers).apply();
    }

    public String fixNumber(String number) {
        String Number = "";
        if (number.length() < 2) return "";
        try {
            Number = number.replaceAll("[ ()#~!-]", "").trim();
            String FirstNumbers = Number.substring(0, 2);

            if (FirstNumbers.equalsIgnoreCase("86")) {
                Number = "+3706" + Number.substring(2, Number.length());
            }
            if (FirstNumbers.equalsIgnoreCase("85")) {
                Number = "+3705" + Number.substring(2, Number.length());
            }
        } catch (Exception ex) {
            Log.d("PhoneContacts", ex.toString());
        }

        return Number;
    }
    private boolean isMyServiceRunning(Class<?> serviceClass,Context ctx) {
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

